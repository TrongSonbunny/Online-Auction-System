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
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Điều khiển màn hình Quản trị hệ thống (Admin Dashboard).
 * Quản lý toàn bộ phiên đấu giá: Xem, Xóa (PENDING) và Kết thúc ép buộc (ACTIVE).
 */
public class AdminController implements Initializable, MessageListener {

  @FXML private HBox titleBar;
  @FXML private Label lblAdminName;
  @FXML private Label lblError; // Nhãn hiển thị lỗi trực quan
  
  @FXML private TableView<Auction> auctionTable;
  @FXML private TableColumn<Auction, String> colId;
  @FXML private TableColumn<Auction, String> colName;
  @FXML private TableColumn<Auction, String> colSeller;
  @FXML private TableColumn<Auction, Double> colPrice;
  @FXML private TableColumn<Auction, String> colWinner; // Cột Người chiến thắng
  @FXML private TableColumn<Auction, String> colTimeRemaining;
  @FXML private TableColumn<Auction, String> colStatus;

  private final ObservableList<Auction> auctionData = FXCollections.observableArrayList();
  private Timeline countdownTimer;
  private AnimationTimer meshGradientTimer;
  private double gradientOffset = 0.0;
  private double offsetX = 0;
  private double offsetY = 0;
  private long lastAutoRefreshTime = 0;

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
    setupTableColumns();
    
    if (App.loggedInEmail != null) {
      if (lblAdminName != null) {
        lblAdminName.setText(App.loggedInEmail.split("@")[0].toUpperCase());
      }
    }

    NetworkClient.getInstance().addListener(this);
    refreshData();
    startCountdownTimer();
    
    Platform.runLater(() -> {
      if (titleBar != null && titleBar.getScene() != null) {
        startMeshGradientAnimation(titleBar.getScene().getRoot());
      }
    });
  }

  private void setupTableColumns() {
    colId.setCellValueFactory(d -> new SimpleStringProperty(
        d.getValue() != null && d.getValue().getAuctionId() != null
            ? d.getValue().getAuctionId() : ""));
            
    colName.setCellValueFactory(d -> new SimpleStringProperty(
        d.getValue() != null && d.getValue().getItem() != null
            ? d.getValue().getItem().getName() : "Unknown"));
            
    colSeller.setCellValueFactory(d -> new SimpleStringProperty(
        d.getValue() != null && d.getValue().getSeller() != null
            ? d.getValue().getSeller().getName() : "Unknown"));
            
    colPrice.setCellValueFactory(d -> new SimpleObjectProperty<>(
        d.getValue() != null ? d.getValue().getCurrentHighestBid() : 0.0));
            
    colTimeRemaining.setCellValueFactory(d ->
        new SimpleStringProperty(calculateTimeRemaining(d.getValue())));
        
    colStatus.setCellValueFactory(d ->
        new SimpleStringProperty(calculateStatus(d.getValue())));
    
    auctionTable.setItems(auctionData);
  }

  private String calculateTimeRemaining(Auction a) {
    if (a == null) {
      return "--";
    }
    String status = a.getStatus() != null ? a.getStatus().name() : "";
    if ("PENDING".equals(status)) {
      return "Chưa mở";
    }
    
    LocalDateTime end = a.getScheduledEndTime();
    if (!"ACTIVE".equals(status) || end == null) {
      return "00:00:00";
    }
    
    LocalDateTime now = LocalDateTime.now();
    if (now.isAfter(end)) {
      return "00:00:00";
    }
    
    long s = java.time.Duration.between(now, end).getSeconds();
    return String.format("%02d:%02d:%02d", s / 3600, (s % 3600) / 60, (s % 60));
  }

  private String calculateStatus(Auction a) {
    if (a == null || a.getStatus() == null) {
      return "";
    }
    String rawStatus = a.getStatus().name();
    if ("PENDING".equals(rawStatus)) {
      return "NOT OPEN";
    }
    if ("ACTIVE".equals(rawStatus)) {
      LocalDateTime end = a.getScheduledEndTime();
      if (end != null && LocalDateTime.now().isAfter(end)) {
        return "CLOSED";
      }
      return "OPEN";
    }
    return "CLOSED";
  }

  /**
   * Tạo hiệu ứng rung lắc (shake) khi người dùng thao tác sai.
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

  @FXML
  private void handleCancelAuction(ActionEvent event) {
    if (lblError != null) {
      lblError.setText("");
    }
    auctionTable.setStyle("");
    
    Auction selected = auctionTable.getSelectionModel().getSelectedItem();
    if (selected == null) {
      if (lblError != null) {
        lblError.setText("LỖI: Vui lòng chọn một phiên trên bảng!");
      }
      auctionTable.setStyle("-fx-border-color: #ff4444; -fx-border-width: 2;");
      triggerShakeAnimation(auctionTable);
      return;
    }
    
    String status = calculateStatus(selected);
    if (!"NOT OPEN".equals(status)) {
      if (lblError != null) {
        lblError.setText("LỖI: Lệnh Xóa chỉ áp dụng cho các phiên chưa mở (NOT OPEN).");
      }
      auctionTable.setStyle("-fx-border-color: #ff4444; -fx-border-width: 2;");
      triggerShakeAnimation(auctionTable);
      return;
    }
    
    if (confirmAction("XÁC NHẬN XÓA",
        "Bạn có chắc chắn muốn XÓA VĨNH VIỄN phiên đấu giá này không?")) {
      ClientMessage cancelReq = ClientMessage.builder()
          .action(ActionType.CANCEL_AUCTION)
          .userId(App.loggedInUserId)
          .auctionId(selected.getAuctionId())
          .build();
      NetworkClient.getInstance().sendMessage(cancelReq);
    }
  }

  @FXML
  private void handleFinishAuction(ActionEvent event) {
    if (lblError != null) {
      lblError.setText("");
    }
    auctionTable.setStyle("");
    
    Auction selected = auctionTable.getSelectionModel().getSelectedItem();
    if (selected == null) {
      if (lblError != null) {
        lblError.setText("LỖI: Vui lòng chọn một phiên trên bảng!");
      }
      auctionTable.setStyle("-fx-border-color: #ff4444; -fx-border-width: 2;");
      triggerShakeAnimation(auctionTable);
      return;
    }
    
    String status = calculateStatus(selected);
    if (!"OPEN".equals(status)) {
      if (lblError != null) {
        lblError.setText("LỖI: Chỉ áp dụng cho phiên đang diễn ra (OPEN).");
      }
      auctionTable.setStyle("-fx-border-color: #ff4444; -fx-border-width: 2;");
      triggerShakeAnimation(auctionTable);
      return;
    }

    if (confirmAction("KẾT THÚC ÉP BUỘC",
        "Bạn có chắc chắn muốn đóng phiên đấu giá này ngay lập tức không? "
            + "Người đang dẫn đầu sẽ lập tức chiến thắng.")) {
      ClientMessage finishReq = ClientMessage.builder()
          .action(ActionType.FINISH_AUCTION)
          .userId(App.loggedInUserId)
          .auctionId(selected.getAuctionId())
          .build();
      NetworkClient.getInstance().sendMessage(finishReq);
    }
  }

  @FXML
  private void handleRefresh(ActionEvent event) {
    if (lblError != null) {
      lblError.setText("");
    }
    auctionTable.setStyle("");
    refreshData();
  }

  private void refreshData() {
    ClientMessage getAuctionsReq = ClientMessage.builder()
        .action(ActionType.GET_ALL_AUCTIONS)
        .build();
    NetworkClient.getInstance().sendMessage(getAuctionsReq);
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
          if (list != null) {
            auctionData.setAll(list);
          }
        }
      } else if (ActionType.CANCEL_AUCTION.name().equals(response.getAction())
          || ActionType.FINISH_AUCTION.name().equals(response.getAction())) {
        if (ServerMessage.STATUS_SUCCESS.equals(response.getStatus())) {
          showAlert("THÀNH CÔNG", "Thao tác quản trị viên đã được thực thi trên hệ thống.");
          refreshData();
        } else {
          showAlert("LỖI", response.getMessage());
        }
      } else if (ServerMessage.ACTION_EVENT.equals(response.getAction())) {
        refreshData();
      }
    });
  }

  private void startCountdownTimer() {
    countdownTimer = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
      boolean shouldRefresh = false;
      if (auctionTable != null && !auctionData.isEmpty()) {
        auctionTable.refresh();
        for (Auction a : auctionData) {
          if ("ACTIVE".equals(a.getStatus().name()) && a.getScheduledEndTime() != null) {
            if (LocalDateTime.now().isAfter(a.getScheduledEndTime())) {
              shouldRefresh = true;
              break;
            }
          }
        }
      }
      if (shouldRefresh && System.currentTimeMillis() - lastAutoRefreshTime > 3000) {
        lastAutoRefreshTime = System.currentTimeMillis();
        refreshData();
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
    
    String cssPath = getClass()
        .getResource("/com/auction/views/style.css").toExternalForm();
    alert.getDialogPane().getStylesheets().add(cssPath);
    alert.getDialogPane().getStyleClass().add("glass-panel");

    alert.showAndWait();
  }

  private boolean confirmAction(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    
    String cssPath = getClass()
        .getResource("/com/auction/views/style.css").toExternalForm();
    alert.getDialogPane().getStylesheets().add(cssPath);
    alert.getDialogPane().getStyleClass().add("glass-panel");

    return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
  }

  @FXML
  private void handleLogout(ActionEvent event) {
    try {
      App.loggedInEmail = null;
      App.loggedInPassword = null;
      App.loggedInUserId = null;
      if (countdownTimer != null) {
        countdownTimer.stop();
      }
      if (meshGradientTimer != null) {
        meshGradientTimer.stop();
      }
      App.setRoot("login");
    } catch (IOException e) {
      showAlert("LỖI", "Không thể đăng xuất.");
    }
  }

  @FXML
  private void handleMinimize(ActionEvent event) {
    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    stage.setIconified(true);
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
        Stage s = (Stage) ((Node) e.getSource()).getScene().getWindow();
        s.setX(e.getScreenX() - offsetX);
        s.setY(e.getScreenY() - offsetY);
      });
    }
  }
}