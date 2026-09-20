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
import javafx.animation.PauseTransition;
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
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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

  @FXML private VBox paymentBox;
  @FXML private ComboBox<String> cbPaymentMethod;
  @FXML private Button btnPay;
  @FXML private Label lblPaymentStatus;

  private final ObservableList<String> historyData = FXCollections.observableArrayList();
  private XYChart.Series<String, Number> priceSeries;
  
  private Timeline countdownTimer;
  private double gradientOffset = 0.0;
  private AnimationTimer meshGradientTimer;
  private Auction currentAuction;
  
  private double lastKnownPrice = 0.0;
  private double offsetX = 0;
  private double offsetY = 0;
  private boolean hasRequestedFinish = false;
  private boolean isFirstLoad = true;

  // Gộp nhiều EVENT real-time liên tiếp (bid dồn dập, cascade auto-bid) thành MỘT
  // lần fetch lại lịch sử/biểu đồ — chống "bão refresh" gây giật lag.
  private final PauseTransition refreshDebounce = new PauseTransition(Duration.millis(250));

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

    if (cbPaymentMethod != null) {
      cbPaymentMethod.setItems(FXCollections.observableArrayList("MOMO", "BANK", "VNPAY"));
      cbPaymentMethod.getSelectionModel().selectFirst();
    }

    NetworkClient.getInstance().addListener(this);

    // Khi hết thời gian chờ (250ms im lặng) mới reconcile lịch sử + biểu đồ một lần.
    refreshDebounce.setOnFinished(e -> {
      requestAuctionsData();
      requestBidHistory();
    });

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
    if (lblLiveName != null) {
      lblLiveName.setText(auction.getItem().getName());
    }
    if (lblLiveSeller != null) {
      lblLiveSeller.setText("Bởi: " 
          + (auction.getSeller() != null 
          ? auction.getSeller().getName() : "Unknown"));
    }
    if (lblLiveCurrentPrice != null) {
      lblLiveCurrentPrice.setText(String.format("%,.0f VNĐ", auction.getCurrentHighestBid()));
    }

    if ("ACTIVE".equals(auction.getStatus().name())) {
      hasRequestedFinish = false; // Reset cờ nếu phiên đang active
    }

    // ĐÃ THÊM: Logic bắt khoảnh khắc vừa kết thúc để nổ Popup!
    // Đã di chuyển xuống đây để giải quyết vi phạm khoảng cách khai báo biến (Checkstyle)
    final boolean wasFinished = this.currentAuction != null 
        && "FINISHED".equals(this.currentAuction.getStatus().name());
    final boolean isFinishedNow = "FINISHED".equals(auction.getStatus().name());

    this.currentAuction = auction;
    
    if (!isFirstLoad && !wasFinished && isFinishedNow) {
      String winnerName = auction.getCurrentHighestBidder() != null
          ? auction.getCurrentHighestBidder().getName() : "Không có ai đặt giá!";
      showAlert("KẾT THÚC PHIÊN ĐẤU GIÁ",
          "Phiên đấu giá đã chính thức khép lại!\n🏆 Người chiến thắng: " + winnerName);
    }

    if (isFinishedNow) {
      if (auction.getCurrentHighestBidder() != null) {
        if (App.loggedInUserId != null
            && App.loggedInUserId.equals(auction.getCurrentHighestBidder().getUserId())) {
          if (lblLiveMyStatus != null) {
            lblLiveMyStatus.setText("🎉 BẠN ĐÃ CHIẾN THẮNG!");
            lblLiveMyStatus.setStyle("-fx-text-fill: #ffd700; -fx-font-weight: bold;");
          }
        } else {
          if (lblLiveMyStatus != null) {
            lblLiveMyStatus.setText("Người thắng: "
                + auction.getCurrentHighestBidder().getName());
            lblLiveMyStatus.setStyle("-fx-text-fill: #ff4444;");
          }
        }
      } else {
        if (lblLiveMyStatus != null) {
          lblLiveMyStatus.setText("Đã kết thúc (Không có người mua)");
          lblLiveMyStatus.setStyle("-fx-text-fill: #888888;");
        }
      }
    } else {
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
    
    // Hiện ô thanh toán nếu phiên đã kết thúc và người đang xem là người thắng.
    updatePaymentVisibility(auction);

    // Đánh dấu đã qua lần tải dữ liệu đầu tiên
    isFirstLoad = false;
  }

  /**
   * Bật/tắt ô thanh toán: chỉ hiện khi phiên đã FINISHED và người đang đăng nhập
   * chính là người thắng cuộc.
   *
   * @param auction phiên đấu giá hiện tại
   */
  private void updatePaymentVisibility(Auction auction) {
    if (paymentBox == null) {
      return;
    }
    boolean isWinner = "FINISHED".equals(auction.getStatus().name())
        && auction.getCurrentHighestBidder() != null
        && App.loggedInUserId != null
        && App.loggedInUserId.equals(auction.getCurrentHighestBidder().getUserId());
    paymentBox.setVisible(isWinner);
    paymentBox.setManaged(isWinner);
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
      DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
      String startTimeStr = currentAuction.getStartTime() != null
          ? currentAuction.getStartTime().format(timeFormatter)
          : LocalDateTime.now().format(timeFormatter);
            
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

  @FXML
  private void handlePay(ActionEvent event) {
    if (currentAuction == null) {
      return;
    }

    String method = cbPaymentMethod != null && cbPaymentMethod.getValue() != null
        ? cbPaymentMethod.getValue() : "MOMO";

    if (btnPay != null) {
      btnPay.setDisable(true);
    }
    if (lblPaymentStatus != null) {
      lblPaymentStatus.setStyle("-fx-text-fill: #d4af37;");
      lblPaymentStatus.setText("⏳ Đang xử lý thanh toán qua " + method + "...");
    }

    ClientMessage payReq = ClientMessage.builder()
        .action(ActionType.PAY)
        .userId(App.loggedInUserId)
        .auctionId(currentAuction.getAuctionId())
        .paymentMethod(method)
        .build();
    NetworkClient.getInstance().sendMessage(payReq);
  }

  /**
   * Xử lý phản hồi PAY thành công từ server (số tiền đã được tính lại trên server).
   *
   * @param response gói phản hồi PAY
   */
  private void handlePaymentResponse(ServerMessage response) {
    String msg = "Thanh toán thành công!";
    if (response.getData() != null) {
      JsonElement el = gson.toJsonTree(response.getData());
      if (el.isJsonObject()) {
        JsonObject obj = el.getAsJsonObject();
        if (obj.has("message") && !obj.get("message").isJsonNull()) {
          msg = obj.get("message").getAsString();
        }
      }
    }

    if (lblPaymentStatus != null) {
      lblPaymentStatus.setStyle("-fx-text-fill: #00ff00; -fx-font-weight: bold;");
      lblPaymentStatus.setText("✅ " + msg);
    }
    if (btnPay != null) {
      btnPay.setDisable(true);
      btnPay.setText("ĐÃ THANH TOÁN");
    }
    showAlert("THANH TOÁN THÀNH CÔNG", msg);
  }

  @Override
  public void onMessageReceived(ServerMessage response) {
    Platform.runLater(() -> {
      if (ActionType.GET_ALL_AUCTIONS.name().equals(response.getAction())) {
        Object dataSource = response.getData() != null
            ? response.getData() : response.getAuctions();
        if (dataSource != null) {
          String json = gson.toJson(dataSource);
          List<Auction> list = gson.fromJson(
              json, new TypeToken<List<Auction>>() {}.getType());
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
        handleRealtimeEvent(response);
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
                "Bạn vừa đặt thành công, nhưng BOT AUTO-BID của người khác đã ngay lập tức "
                    + "đẩy giá lên cao hơn!");
          }
          txtLiveBidAmount.clear();
          refreshDebounce.playFromStart();
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
      } else if (ActionType.PAY.name().equals(response.getAction())) {
        if (ServerMessage.STATUS_SUCCESS.equals(response.getStatus())) {
          handlePaymentResponse(response);
        }
      } else if ("EXECUTION_ERROR".equals(response.getAction())) {
        // Nếu một lượt thanh toán đang chờ (status bắt đầu bằng ⏳) mà server báo
        // lỗi → mở lại nút để người dùng thử lại với phương thức khác.
        if (lblPaymentStatus != null && lblPaymentStatus.getText() != null
            && lblPaymentStatus.getText().startsWith("⏳")) {
          if (btnPay != null) {
            btnPay.setDisable(false);
          }
          lblPaymentStatus.setStyle("-fx-text-fill: #ff4444;");
          lblPaymentStatus.setText("❌ " + response.getMessage());
        }

        // LỚP PHÒNG THỦ 2: Bắt trọn vẹn lỗi Hệ Thống từ Server
        // (VD: Phiên đấu giá đóng, ID sai...)
        if (lblError != null) {
          lblError.setText("LỖI TỪ SERVER: " + response.getMessage());
          triggerShakeAnimation(lblError);
        } else {
          showAlert("LỖI HỆ THỐNG", response.getMessage());
        }
      }
    });
  }

  /**
   * Xử lý EVENT real-time đẩy từ server (qua FrontendNotificationObserver).
   * Cập nhật giá tức thì cho cảm giác mượt, rồi gộp các event để reconcile một lần.
   *
   * @param response gói EVENT từ server
   */
  private void handleRealtimeEvent(ServerMessage response) {
    JsonObject evt = parseEventMessage(response.getMessage());

    // Thông báo cá nhân (seller/winner) đã được xử lý ở nơi khác (popup thắng,
    // trạng thái dẫn đầu/bị vượt) — phòng Live bỏ qua để tránh trùng lặp.
    if (evt != null && evt.has("recipient")) {
      return;
    }

    // Event dữ liệu: hiển thị ngay giá mới (không chờ round-trip), sau đó debounce
    // để vẽ lại biểu đồ + lịch sử một lần duy nhất dù có bao nhiêu event dồn dập.
    if (evt != null) {
      applyInstantPrice(evt);
    }
    refreshDebounce.playFromStart();
  }

  private JsonObject parseEventMessage(String message) {
    if (message == null || message.isBlank()) {
      return null;
    }
    try {
      return gson.fromJson(message, JsonObject.class);
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Cập nhật ngay nhãn giá từ payload của event (nếu đúng phiên đang xem).
   *
   * @param evt JSON event đã parse
   */
  private void applyInstantPrice(JsonObject evt) {
    if (targetAuctionId == null) {
      return;
    }
    if (evt.has("auctionId") && !evt.get("auctionId").isJsonNull()
        && !targetAuctionId.equals(evt.get("auctionId").getAsString())) {
      return;
    }
    if (!evt.has("payload") || evt.get("payload").isJsonNull()
        || !evt.get("payload").isJsonObject()) {
      return;
    }

    JsonObject payload = evt.getAsJsonObject("payload");
    Double price = null;
    if (payload.has("bidAmount") && !payload.get("bidAmount").isJsonNull()) {
      price = payload.get("bidAmount").getAsDouble();
    } else if (payload.has("currentHighestBid")
        && !payload.get("currentHighestBid").isJsonNull()) {
      price = payload.get("currentHighestBid").getAsDouble();
    }

    if (price != null && price > 0) {
      lastKnownPrice = price;
      if (lblLiveCurrentPrice != null) {
        lblLiveCurrentPrice.setText(String.format("%,.0f VNĐ", price));
      }
    }
  }

  private void startCountdownTimer() {
    countdownTimer = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
      if (currentAuction != null) {
        LocalDateTime end = currentAuction.getScheduledEndTime();
        if (end == null || LocalDateTime.now().isAfter(end)) {
          if (lblCountdownLive != null) {
            lblCountdownLive.setText("00:00:00");

            if (!hasRequestedFinish && "ACTIVE".equals(currentAuction.getStatus().name())) {
              hasRequestedFinish = true;
              requestAuctionsData();
            }
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
      private long lastUpdate = 0;

      @Override
      public void handle(long now) {
        // Giới hạn ~12fps. setStyle() buộc JavaFX parse lại CSS của cả cây node —
        // chạy mỗi frame (~60fps) là nguyên nhân giật/lag chính. Throttle ở đây
        // giảm tải ~5 lần mà mắt thường gần như không phân biệt được.
        if (now - lastUpdate < 80_000_000L) {
          return;
        }
        lastUpdate = now;
        gradientOffset += 0.0025;
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