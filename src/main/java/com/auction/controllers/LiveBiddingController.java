package com.auction.controllers;

import com.auction.App;
import com.auction.models.auction.Auction;
import com.auction.models.payment.PaymentStrategy;
import com.auction.models.user.permission.PermissionStrategy;
import com.auction.network.ActionType;
import com.auction.network.ClientMessage;
import com.auction.network.MessageListener;
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
import javafx.animation.TranslateTransition;
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
public class LiveBiddingController implements Initializable, MessageListener {

  public static String targetAuctionId = null;

  @FXML private HBox titleBar;
  @FXML private Label lblLiveName;
  @FXML private Label lblLiveSeller;
  @FXML private Label lblCountdownLive;
  @FXML private Label lblLiveCurrentPrice;
  @FXML private Label lblLiveMyStatus;
  @FXML private Label lblLiveTotalBids;
  @FXML private Label lblError; // Label hiển thị lỗi giao diện
  
  @FXML private LineChart<String, Number> priceChart;
  @FXML private CategoryAxis categoryAxis;
  @FXML private NumberAxis numberAxis;
  @FXML private ListView<String> listLiveHistory;
  
  @FXML private TextField txtLiveBidAmount;
  @FXML private TextField txtMaxBid;
  @FXML private TextField txtIncrement;

  private final ObservableList<String> historyData = FXCollections.observableArrayList();
  private XYChart.Series<String, Number> priceSeries;
  
  private Timeline countdownTimer;
  private double gradientOffset = 0.0;
  private AnimationTimer meshGradientTimer;
  private Auction currentAuction;
  
  private double lastKnownPrice = 0.0; 
  private double offsetX = 0;
  private double offsetY = 0;

  private final Gson gson = new GsonBuilder()
      .registerTypeAdapter(LocalDateTime.class,
          (JsonDeserializer<LocalDateTime>) (json, type, ctx) -> {
            try {
              return LocalDateTime.parse(json.getAsString(),
                  DateTimeFormatter.ISO_LOCAL_DATE_TIME);
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
      if (lblLiveName != null) {
        lblLiveName.setText("Lỗi: Không nhận được mã sản phẩm.");
      }
    }
    
    Platform.runLater(() -> {
      if (titleBar != null && titleBar.getScene() != null) {
        startMeshGradientAnimation(titleBar.getScene().getRoot());
      }
    });
  }

  private void requestAuctionsData() {
    ClientMessage getReq = ClientMessage.builder().action(ActionType.GET_ALL_AUCTIONS).build();
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
    
    if (lblLiveName != null) {
      lblLiveName.setText(auction.getItem().getName());
    }
    if (lblLiveSeller != null) {
      lblLiveSeller.setText("Bởi: " + (auction.getSeller() != null
          ? auction.getSeller().getName() : "Unknown"));
    }
    if (lblLiveCurrentPrice != null) {
      lblLiveCurrentPrice.setText(String.format("%,.0f VNĐ", auction.getCurrentHighestBid()));
    }
    
    if (auction.getCurrentHighestBidder() != null) {
      if (App.loggedInUserId != null
          && App.loggedInUserId.equals(auction.getCurrentHighestBidder().getUserId())) {
        if (lblLiveMyStatus != null) {
          lblLiveMyStatus.setText("Đang dẫn đầu!");
          lblLiveMyStatus.setStyle("-fx-text-fill: #00ff00;");
        }
      } else {
        if (lblLiveMyStatus != null) {
          lblLiveMyStatus.setText("Bị vượt giá!");
          lblLiveMyStatus.setStyle("-fx-text-fill: #ff4444;");
        }
      }
    } else {
      if (lblLiveMyStatus != null) {
        lblLiveMyStatus.setText("Chưa ra giá");
        lblLiveMyStatus.setStyle("-fx-text-fill: white;");
      }
    }
  }

  /**
   * Đọc dữ liệu phẳng an toàn tuyệt đối từ gói tin DTO của Backend.
   *
   * @param transactions danh sách gói dữ liệu JSON phẳng
   */
  private void renderHistoryAndChart(List<JsonObject> transactions) {
    if (currentAuction == null) {
      return;
    }
    
    Platform.runLater(() -> {
      priceSeries.getData().clear();
      historyData.clear();
      
      double basePrice = currentAuction.getStartingPrice();
      String startTimeStr = currentAuction.getStartTime() != null 
          ? currentAuction.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
          : LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            
      priceSeries.getData().add(new XYChart.Data<>(startTimeStr, basePrice));
      historyData.add(String.format("[%s] Hệ thống: Mở phiên giá %,.0f VNĐ",
          startTimeStr, basePrice));
      
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
            time = LocalDateTime.parse(tx.get("createdAt").getAsString(),
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
      
      if (lblLiveTotalBids != null) {
        lblLiveTotalBids.setText(String.valueOf(totalBids));
      }
      
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

  /**
   * Tạo hiệu ứng rung lắc (shake) khi có lỗi nhập liệu.
   *
   * @param node thành phần giao diện cần rung lắc
   */
  private void triggerShakeAnimation(Node node) {
    TranslateTransition shake = new TranslateTransition(Duration.millis(100), node);
    shake.setFromX(0);
    shake.setToX(5);
    shake.setCycleCount(4);
    shake.setAutoReverse(true);
    shake.play();
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
    if (lblError != null) {
      lblError.setText("");
    }
    txtLiveBidAmount.setStyle("");

    if (currentAuction == null) {
      if (lblError != null) {
        lblError.setText("LỖI: Chưa có thông tin phiên đấu giá!");
      }
      return;
    }

    if (txtLiveBidAmount.getText().trim().isEmpty()) {
      if (lblError != null) {
        lblError.setText("LỖI: Vui lòng nhập mức giá bạn muốn đặt!");
      }
      txtLiveBidAmount.setStyle("-fx-border-color: #ff4444; -fx-border-width: 2;");
      triggerShakeAnimation(txtLiveBidAmount);
      return;
    }

    try {
      double amount = Double.parseDouble(txtLiveBidAmount.getText());
      
      // LỚP PHÒNG THỦ 1: Validate Local trước khi gửi lên Server để phản hồi tức thì
      if (amount <= lastKnownPrice) {
        if (lblError != null) {
          lblError.setText(String.format(
              "LỖI: Mức giá đặt phải lớn hơn giá hiện tại (%,.0f VNĐ)!", lastKnownPrice));
        }
        txtLiveBidAmount.setStyle("-fx-border-color: #ff4444; -fx-border-width: 2;");
        triggerShakeAnimation(txtLiveBidAmount);
        return;
      }

      ClientMessage bidReq = ClientMessage.builder()
          .action(ActionType.BID)
          .userId(App.loggedInUserId)
          .auctionId(currentAuction.getAuctionId())
          .bidAmount(amount)
          .build();
      NetworkClient.getInstance().sendMessage(bidReq);
      
    } catch (NumberFormatException e) {
      if (lblError != null) {
        lblError.setText("LỖI: Dữ liệu nhập vào phải là số hợp lệ!");
      }
      txtLiveBidAmount.setStyle("-fx-border-color: #ff4444; -fx-border-width: 2;");
      triggerShakeAnimation(txtLiveBidAmount);
    }
  }

  @FXML
  private void handleRegisterAutoBid(ActionEvent event) {
    if (lblError != null) {
      lblError.setText("");
    }
    txtMaxBid.setStyle("");
    txtIncrement.setStyle("");

    if (currentAuction == null) {
      if (lblError != null) {
        lblError.setText("LỖI: Chưa có thông tin phiên đấu giá!");
      }
      return;
    }

    boolean hasError = false;

    if (txtMaxBid.getText().trim().isEmpty()) {
      txtMaxBid.setStyle("-fx-border-color: #ff4444; -fx-border-width: 2;");
      triggerShakeAnimation(txtMaxBid);
      hasError = true;
    }

    if (txtIncrement.getText().trim().isEmpty()) {
      txtIncrement.setStyle("-fx-border-color: #ff4444; -fx-border-width: 2;");
      triggerShakeAnimation(txtIncrement);
      hasError = true;
    }

    if (hasError) {
      if (lblError != null) {
        lblError.setText("LỖI: Vui lòng điền đầy đủ Giới hạn và Bước giá của BOT!");
      }
      return;
    }

    try {
      double maxBid = Double.parseDouble(txtMaxBid.getText());
      double increment = Double.parseDouble(txtIncrement.getText());
      
      // LỚP PHÒNG THỦ 1: Bắt lỗi ngay tại Client
      if (maxBid <= lastKnownPrice) {
        if (lblError != null) {
          lblError.setText(String.format(
              "LỖI: Giới hạn túi tiền (Max Bid) phải cao hơn giá hiện tại (%,.0f VNĐ)!",
              lastKnownPrice));
        }
        txtMaxBid.setStyle("-fx-border-color: #ff4444; -fx-border-width: 2;");
        triggerShakeAnimation(txtMaxBid);
        return;
      }

      ClientMessage autoReq = ClientMessage.builder()
          .action(ActionType.REGISTER_AUTO_BID)
          .userId(App.loggedInUserId)
          .auctionId(currentAuction.getAuctionId())
          .maxBid(maxBid)
          .increment(increment)
          .build();
      NetworkClient.getInstance().sendMessage(autoReq);
      
    } catch (NumberFormatException e) {
      if (lblError != null) {
        lblError.setText("LỖI: Dữ liệu cấu hình BOT không hợp lệ (Phải là số)!");
      }
      txtMaxBid.setStyle("-fx-border-color: #ff4444; -fx-border-width: 2;");
      txtIncrement.setStyle("-fx-border-color: #ff4444; -fx-border-width: 2;");
      triggerShakeAnimation(txtMaxBid);
      triggerShakeAnimation(txtIncrement);
    }
  }

  @Override
  public void onMessageReceived(ServerMessage response) {
    Platform.runLater(() -> {
      if (ActionType.GET_ALL_AUCTIONS.name().equals(response.getAction())) {
        Object dataSource = response.getData() != null
            ? response.getData() : response.getAuctions();
        if (dataSource != null) {
          String json = gson.toJson(dataSource);
          List<Auction> list = gson.fromJson(json, new TypeToken<List<Auction>>() {}.getType());
          if (list != null && targetAuctionId != null) {
            Optional<Auction> target = list.stream()
                .filter(a -> a.getAuctionId().equals(targetAuctionId)).findFirst();
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
                if (bidResult.get("autoBidTransactions").getAsJsonArray().size() > 0) {
                  outbidByAuto = true;
                }
              }
            }
          }
          if (outbidByAuto) {
            showAlert("BỊ CƯỚP HÀNG",
                "Bạn vừa đặt thành công, nhưng BOT AUTO-BID đã ngay lập tức đẩy giá lên cao hơn!");
          }
          txtLiveBidAmount.clear();
          requestAuctionsData();
          requestBidHistory();
        } else {
          // Xử lý lỗi trả về nếu có
          if (lblError != null) {
            lblError.setText("LỖI: " + response.getMessage());
            triggerShakeAnimation(lblError);
          } else {
            showAlert("TỪ CHỐI", response.getMessage());
          }
        }
      } else if (ActionType.REGISTER_AUTO_BID.name().equals(response.getAction())) {
        if (ServerMessage.STATUS_SUCCESS.equals(response.getStatus())) {
          showAlert("BOT ĐÃ KÍCH HOẠT", "Hệ thống sẽ tự động cạnh tranh giá thay bạn!");
          txtMaxBid.clear();
          txtIncrement.clear();
        } else {
          if (lblError != null) {
            lblError.setText("LỖI: " + response.getMessage());
            triggerShakeAnimation(lblError);
          } else {
            showAlert("TỪ CHỐI BOT", response.getMessage());
          }
        }
      } else if ("EXECUTION_ERROR".equals(response.getAction())) {
        // LỚP PHÒNG THỦ 2: Bắt trọn vẹn lỗi Hệ Thống từ Server (VD: Phiên đấu giá đóng, ID sai...)
        if (lblError != null) {
          lblError.setText("LỖI TỪ SERVER: " + response.getMessage());
          triggerShakeAnimation(lblError);
        } else {
          showAlert("LỖI HỆ THỐNG", response.getMessage());
        }
      }
    });
  }

  private void startCountdownTimer() {
    countdownTimer = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
      if (currentAuction != null) {
        LocalDateTime end = currentAuction.getScheduledEndTime();
        if (end == null || LocalDateTime.now().isAfter(end)) {
          if (lblCountdownLive != null) {
            lblCountdownLive.setText("00:00:00");
          }
        } else {
          long s = java.time.Duration.between(LocalDateTime.now(), end).getSeconds();
          if (lblCountdownLive != null) {
            lblCountdownLive.setText(String.format("%02d:%02d:%02d",
                s / 3600, (s % 3600) / 60, (s % 60)));
          }
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
        targetNode.setStyle(String.format(Locale.US,
            "-fx-background-color: linear-gradient(to bottom right, #0a0a0a, "
                + "rgba(26, 21, 5, %f), rgba(5, 5, 5, %f));",
            (Math.sin(gradientOffset) * 0.1 + 0.9),
            (Math.cos(gradientOffset) * 0.1 + 0.9)));
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
    ((Stage) ((Node) event.getSource()).getScene().getWindow()).setIconified(true);
  }
  
  @FXML
  private void handleMaximize(ActionEvent event) {
    Stage s = (Stage) ((Node) event.getSource()).getScene().getWindow();
    s.setMaximized(!s.isMaximized());
  }
  
  @FXML
  private void handleClose(ActionEvent event) { 
    if (meshGradientTimer != null) {
      meshGradientTimer.stop();
    }
    if (countdownTimer != null) {
      countdownTimer.stop();
    }
    targetAuctionId = null;
    try {
      App.setRoot("primary");
    } catch (IOException e) {
      showAlert("LỖI", "Không thể quay lại màn hình chính.");
    }
  }
  
  private void setupUndecoratedWindowHandle() {
    if (titleBar != null) {
      titleBar.setOnMousePressed(e -> {
        offsetX = e.getSceneX();
        offsetY = e.getSceneY();
      });
      titleBar.setOnMouseDragged(e -> {
        Stage s = (Stage) ((Node) e.getSource()).getScene().getWindow();
        s.setX(e.getScreenX() - offsetX);
        s.setY(e.getScreenY() - offsetY);
      });
    }
  }
}