package com.auction.controllers;

import com.auction.App;
import com.auction.models.auction.Auction;
import com.auction.models.payment.PaymentStrategy;
import com.auction.models.user.permission.PermissionStrategy;
import com.auction.network.ActionType;
import com.auction.network.ClientMessage;
import com.auction.network.NetworkClient;
import com.auction.network.ServerMessage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Controller cho Phòng Live Bidding Độc Lập.
 * Sử dụng Server-Side State làm Nguồn Chân Lý Duy Nhất (Single Source of Truth)
 * để đồng bộ hóa số lượt Bid, Lịch sử và Biểu đồ cho tất cả Client.
 */
public class LiveBiddingController implements Initializable, NetworkClient.MessageListener {

  public static String targetAuctionId = null;

  @FXML
  private HBox titleBar;
  @FXML
  private Label lblLiveName;
  @FXML
  private Label lblLiveSeller;
  @FXML
  private Label lblCountdownLive;
  @FXML
  private Label lblLiveCurrentPrice;
  @FXML
  private Label lblLiveMyStatus;
  @FXML
  private Label lblLiveTotalBids;

  @FXML
  private LineChart<String, Number> priceChart;
  @FXML
  private CategoryAxis xaxis;
  @FXML
  private NumberAxis yaxis;
  @FXML
  private ListView<String> listLiveHistory;

  @FXML
  private TextField txtLiveBidAmount;
  @FXML
  private TextField txtMaxBid;
  @FXML
  private TextField txtIncrement;

  private final ObservableList<String> historyData = FXCollections.observableArrayList();
  private XYChart.Series<String, Number> priceSeries;

  private Timeline countdownTimer;
  private AnimationTimer meshGradientTimer;
  private Auction currentAuction;

  private double lastKnownPrice = 0.0;
  private double offsetX = 0.0;
  private double offsetY = 0.0;
  private double gradientOffset = 0.0;

  private final Gson gson = new GsonBuilder()
      .registerTypeAdapter(LocalDateTime.class,
          (JsonDeserializer<LocalDateTime>) (json, type, ctx) -> {
            try {
              return LocalDateTime.parse(
                  json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (Exception e) {
              return LocalDateTime.now();
            }
          })
      .registerTypeAdapter(PermissionStrategy.class,
          (JsonDeserializer<PermissionStrategy>) (json, type, ctx) -> null)
      .registerTypeAdapter(PaymentStrategy.class,
          (JsonDeserializer<PaymentStrategy>) (json, type, ctx) -> null)
      .create();

  @Override
  public void initialize(URL url, ResourceBundle rb) {
    setupUndecoratedWindowHandle();

    priceSeries = new XYChart.Series<>();
    priceChart.getData().add(priceSeries);
    listLiveHistory.setItems(historyData);

    NetworkClient.getInstance().addListener(this);

    if (targetAuctionId != null) {
      requestAuctionsData();
      requestBidHistory();
      startCountdownTimer();
    } else {
      lblLiveName.setText("Lỗi: Không nhận được mã sản phẩm.");
    }

    Platform.runLater(() -> {
      if (titleBar != null && titleBar.getScene() != null) {
        startMeshGradientAnimation(titleBar.getScene().getRoot());
      }
    });
  }

  private void requestAuctionsData() {
    ClientMessage getReq = ClientMessage.builder()
        .action(ActionType.GET_ALL_AUCTIONS)
        .build();
    NetworkClient.getInstance().sendMessage(getReq);
  }

  private void requestBidHistory() {
    ClientMessage getHistoryReq = ClientMessage.builder()
        .action(ActionType.valueOf("GET_BID_HISTORY"))
        .auctionId(targetAuctionId)
        .build();
    NetworkClient.getInstance().sendMessage(getHistoryReq);
  }

  private void updateLiveRoom(Auction auction) {
    this.currentAuction = auction;

    lblLiveName.setText(auction.getItem().getName());
    String sellerName = (auction.getSeller() != null) 
        ? auction.getSeller().getName() : "Unknown";
    lblLiveSeller.setText("Bởi: " + sellerName);
    lblLiveCurrentPrice.setText(String.format("%,.0f VNĐ", auction.getCurrentHighestBid()));

    if (auction.getCurrentHighestBidder() != null) {
      String highestBidderId = auction.getCurrentHighestBidder().getUserId();
      if (App.loggedInUserId != null && App.loggedInUserId.equals(highestBidderId)) {
        lblLiveMyStatus.setText("Đang dẫn đầu!");
        lblLiveMyStatus.setStyle("-fx-text-fill: #00ff00;");
      } else {
        lblLiveMyStatus.setText("Bị vượt giá!");
        lblLiveMyStatus.setStyle("-fx-text-fill: #ff4444;");
      }
    } else {
      lblLiveMyStatus.setText("Chưa ra giá");
      lblLiveMyStatus.setStyle("-fx-text-fill: white;");
    }
  }

  /**
   * ĐÃ FIX TẬN GỐC: Đọc dữ liệu phẳng an toàn tuyệt đối từ gói tin DTO của Backend.
   */
  private void renderHistoryAndChart(List<JsonObject> transactions) {
    if (currentAuction == null) {
      return;
    }

    Platform.runLater(() -> {
      priceSeries.getData().clear();
      historyData.clear();

      // 1. Ghi lại điểm gốc (Giá khởi điểm)
      double basePrice = currentAuction.getStartingPrice();
      String startTimeStr = currentAuction.getStartTime() != null
          ? currentAuction.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
          : LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

      priceSeries.getData().add(new XYChart.Data<>(startTimeStr, basePrice));
      historyData.add(String.format("[%s] Hệ thống: Mở phiên giá %,.0f VNĐ", 
          startTimeStr, basePrice));

      // 2. Duyệt danh sách giao dịch an toàn từ DTO phẳng
      int totalBids = 0;
      for (JsonObject tx : transactions) {
        if (tx == null || tx.isJsonNull()) {
          continue;
        }

        totalBids++;

        double amount = tx.has("bidAmount") ? tx.get("bidAmount").getAsDouble() : 0.0;
        String bidderName = tx.has("bidderName") 
            ? tx.get("bidderName").getAsString() : "Ẩn danh";

        LocalDateTime time = LocalDateTime.now();
        if (tx.has("createdAt") && !tx.get("createdAt").isJsonNull()) {
          try {
            String timeString = tx.get("createdAt").getAsString();
            time = LocalDateTime.parse(timeString, 
                DateTimeFormatter.ISO_LOCAL_DATE_TIME);
          } catch (Exception e) {
            // Fallback
          }
        }

        String exactTime = time.format(DateTimeFormatter.ofPattern("dd/MM HH:mm:ss"));
        String displayTime = time.format(DateTimeFormatter.ofPattern("HH:mm:ss")) 
            + " ".repeat(totalBids % 20);

        priceSeries.getData().add(new XYChart.Data<>(displayTime, amount));
        String historyLine = String.format("[%s] %s ra giá %,.0f VNĐ", 
            exactTime, bidderName, amount);
        historyData.add(0, historyLine);
      }

      if (priceSeries.getData().size() > 30) {
        priceSeries.getData().remove(0, priceSeries.getData().size() - 30);
      }

      lblLiveTotalBids.setText(String.valueOf(totalBids));

      if (totalBids > 0) {
        JsonObject lastTx = transactions.get(transactions.size() - 1);
        if (lastTx != null && lastTx.has("bidAmount")) {
          lastKnownPrice = lastTx.get("bidAmount").getAsDouble();
        }
      } else {
        lastKnownPrice = basePrice;
      }
    });
  }

  // --- CONTROLS ---

  @FXML
  private void handleBackToRadar(ActionEvent event) {
    try {
      if (countdownTimer != null) {
        countdownTimer.stop();
      }
      if (meshGradientTimer != null) {
        meshGradientTimer.stop();
      }
      targetAuctionId = null;
      App.setRoot("primary");
    } catch (IOException e) {
      showAlert("LỖI", "Không thể quay lại Radar.");
    }
  }

  @FXML
  private void handleQuickBid50k(ActionEvent event) {
    txtLiveBidAmount.setText(String.format("%.0f", lastKnownPrice + 50000));
  }

  @FXML
  private void handleQuickBid100k(ActionEvent event) {
    txtLiveBidAmount.setText(String.format("%.0f", lastKnownPrice + 100000));
  }

  @FXML
  private void handleQuickBid500k(ActionEvent event) {
    txtLiveBidAmount.setText(String.format("%.0f", lastKnownPrice + 500000));
  }

  @FXML
  private void handlePlaceLiveBid(ActionEvent event) {
    if (currentAuction == null) {
      return;
    }
    try {
      double amount = Double.parseDouble(txtLiveBidAmount.getText());
      ClientMessage bidReq = ClientMessage.builder()
          .action(ActionType.BID)
          .userId(App.loggedInUserId)
          .auctionId(currentAuction.getAuctionId())
          .bidAmount(amount)
          .build();
      NetworkClient.getInstance().sendMessage(bidReq);
    } catch (Exception e) {
      showAlert("CẢNH BÁO", "Dữ liệu nhập vào không hợp lệ!");
    }
  }

  @FXML
  private void handleRegisterAutoBid(ActionEvent event) {
    if (currentAuction == null) {
      return;
    }
    try {
      double maxBid = Double.parseDouble(txtMaxBid.getText());
      double increment = Double.parseDouble(txtIncrement.getText());
      ClientMessage autoReq = ClientMessage.builder()
          .action(ActionType.REGISTER_AUTO_BID)
          .userId(App.loggedInUserId)
          .auctionId(currentAuction.getAuctionId())
          .maxBid(maxBid)
          .increment(increment)
          .build();
      NetworkClient.getInstance().sendMessage(autoReq);
    } catch (Exception e) {
      showAlert("CẢNH BÁO", "Dữ liệu BOT không hợp lệ!");
    }
  }

  @Override
  public void onMessageReceived(ServerMessage response) {
    Platform.runLater(() -> {
      if (ActionType.GET_ALL_AUCTIONS.name().equals(response.getAction())) {
        Object dataSource = (response.getData() != null) 
            ? response.getData() : response.getAuctions();
        if (dataSource != null) {
          String json = gson.toJson(dataSource);
          List<Auction> list = gson.fromJson(json, 
              new TypeToken<List<Auction>>() {}.getType());
          if (list != null && targetAuctionId != null) {
            Optional<Auction> target = list.stream()
                .filter(a -> a.getAuctionId().equals(targetAuctionId))
                .findFirst();
            target.ifPresent(this::updateLiveRoom);
          }
        }
      } else if ("GET_BID_HISTORY".equals(response.getAction())) {
        if (response.getData() != null) {
          String json = gson.toJson(response.getData());
          List<JsonObject> txs = gson.fromJson(json, 
              new TypeToken<List<JsonObject>>() {}.getType());
          if (txs != null) {
            renderHistoryAndChart(txs);
          }
        }
      } else if (ServerMessage.ACTION_EVENT.equals(response.getAction())) {
        requestAuctionsData();
        requestBidHistory();
      } else if (ActionType.BID.name().equals(response.getAction())) {
        if (ServerMessage.STATUS_SUCCESS.equals(response.getStatus())) {
          boolean outbidByAuto = false;
          if (response.getData() != null) {
            JsonElement jsonElement = gson.toJsonTree(response.getData());
            if (jsonElement.isJsonObject()) {
              JsonObject bidResult = jsonElement.getAsJsonObject();
              if (bidResult.has("autoBidTransactions") 
                  && bidResult.get("autoBidTransactions").isJsonArray()) {
                int size = bidResult.get("autoBidTransactions")
                    .getAsJsonArray().size();
                if (size > 0) {
                  outbidByAuto = true;
                }
              }
            }
          }
          if (outbidByAuto) {
            showAlert("BỊ CƯỚP HÀNG", 
                "Bạn vừa đặt thành công, nhưng BOT AUTO-BID "
                + "của người khác đã đẩy giá lên cao hơn!");
          }
          txtLiveBidAmount.clear();
          requestAuctionsData();
          requestBidHistory();
        } else {
          showAlert("TỪ CHỐI", response.getMessage());
        }
      } else if (ActionType.REGISTER_AUTO_BID.name().equals(response.getAction())) {
        if (ServerMessage.STATUS_SUCCESS.equals(response.getStatus())) {
          showAlert("BOT ĐÃ KÍCH HOẠT", "Hệ thống sẽ tự động cạnh tranh giá thay bạn!");
          txtMaxBid.clear();
          txtIncrement.clear();
        } else {
          showAlert("TỪ CHỐI BOT", response.getMessage());
        }
      }
    });
  }

  private void startCountdownTimer() {
    countdownTimer = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
      if (currentAuction != null) {
        LocalDateTime end = currentAuction.getScheduledEndTime();
        if (end == null || LocalDateTime.now().isAfter(end)) {
          lblCountdownLive.setText("00:00:00");
        } else {
          long s = java.time.Duration.between(LocalDateTime.now(), end).getSeconds();
          lblCountdownLive.setText(String.format("%02d:%02d:%02d", 
              s / 3600, (s % 3600) / 60, (s % 60)));
        }
      }
    }));
    countdownTimer.setCycleCount(Animation.INDEFINITE);
    countdownTimer.play();
  }

  private void startMeshGradientAnimation(Node targetNode) {
    meshGradientTimer = new AnimationTimer() {
      @Override
      public void handle(long now) {
        gradientOffset += 0.0005;
        double color1 = Math.sin(gradientOffset) * 0.1 + 0.9;
        double color2 = Math.cos(gradientOffset) * 0.1 + 0.9;
        String style = String.format(Locale.US,
            "-fx-background-color: linear-gradient(to bottom right, #0a0a0a, "
                + "rgba(26, 21, 5, %f), rgba(5, 5, 5, %f));",
            color1, color2);
        targetNode.setStyle(style);
      }
    };
    meshGradientTimer.start();
  }

  private void showAlert(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    String cssPath = getClass().getResource("/com/auction/views/style.css").toExternalForm();
    alert.getDialogPane().getStylesheets().add(cssPath);
    alert.getDialogPane().getStyleClass().add("glass-panel");
    alert.showAndWait();
  }

  @FXML
  private void handleMinimize(ActionEvent event) {
    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    stage.setIconified(true);
  }

  @FXML
  private void handleMaximize(ActionEvent event) {
    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    stage.setMaximized(!stage.isMaximized());
  }

  @FXML
  private void handleClose(ActionEvent event) {
    if (meshGradientTimer != null) {
      meshGradientTimer.stop();
    }
    if (countdownTimer != null) {
      countdownTimer.stop();
    }
    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    stage.close();
  }

  private void setupUndecoratedWindowHandle() {
    if (titleBar != null) {
      titleBar.setOnMousePressed(e -> {
        offsetX = e.getSceneX();
        offsetY = e.getSceneY();
      });
      titleBar.setOnMouseDragged(e -> {
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        stage.setX(e.getScreenX() - offsetX);
        stage.setY(e.getScreenY() - offsetY);
      });
    }
  }
}