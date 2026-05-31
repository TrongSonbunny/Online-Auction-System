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
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Điều khiển màn hình Radar dành cho Bidder (Liệt kê & Xem chi tiết).
 * Tích hợp Load ảnh minh họa theo ItemCategory.
 */
public class PrimaryController implements Initializable, MessageListener {

  @FXML private HBox titleBar;
  @FXML private TableView<Auction> productTable;
  @FXML private TableColumn<Auction, String> colId;
  @FXML private TableColumn<Auction, String> colName;
  @FXML private TableColumn<Auction, Double> colPrice;
  @FXML private TableColumn<Auction, String> colTimeRemaining;
  @FXML private TableColumn<Auction, String> colStatus;
  
  @FXML private VBox placeholderBox;
  @FXML private ScrollPane detailScrollPane;
  @FXML private ImageView imgProduct;
  @FXML private Label lblDetailName;
  @FXML private Label lblDetailStatus;
  @FXML private Label lblDetailDesc;
  @FXML private Label lblDetailCategory;
  @FXML private Label lblDetailCondition;
  @FXML private Label lblDetailStartPrice;
  @FXML private Label lblDetailEstPrice;
  @FXML private Label lblDetailSeller;
  @FXML private Button btnJoinLive;

  private final ObservableList<Auction> auctionData = FXCollections.observableArrayList();
  private Timeline countdownTimer;
  private AnimationTimer meshGradientTimer;
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
    setupTableColumns();
    
    productTable.setItems(auctionData);
    productTable.getSelectionModel().selectedItemProperty().addListener(
        (obs, oldSel, newSel) -> {
          switchDetailView(newSel);
        });

    NetworkClient.getInstance().addListener(this);
    refreshData();
    startCountdownTimer();
  }

  private void setupTableColumns() {
    colId.setCellValueFactory(d -> new SimpleStringProperty(
        d.getValue() != null && d.getValue().getAuctionId() != null
            ? d.getValue().getAuctionId() : ""));
            
    colName.setCellValueFactory(d -> new SimpleStringProperty(
        d.getValue() != null && d.getValue().getItem() != null
            ? d.getValue().getItem().getName() : "Unknown"));
            
    colPrice.setCellValueFactory(d -> new SimpleObjectProperty<>(
        d.getValue() != null ? d.getValue().getCurrentHighestBid() : 0.0));
        
    colTimeRemaining.setCellValueFactory(d -> 
        new SimpleStringProperty(calculateTimeRemaining(d.getValue())));
        
    colStatus.setCellValueFactory(d -> 
        new SimpleStringProperty(calculateStatus(d.getValue())));
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

  private void switchDetailView(Auction auction) {
    if (auction == null || auction.getItem() == null) {
      placeholderBox.setVisible(true);
      placeholderBox.setManaged(true);
      detailScrollPane.setVisible(false);
      detailScrollPane.setManaged(false);
      return;
    }
    placeholderBox.setVisible(false);
    placeholderBox.setManaged(false);
    detailScrollPane.setVisible(true);
    detailScrollPane.setManaged(true);
    updateDetailValues(auction);
  }

  /**
   * Cập nhật thông tin chi tiết và nạp ảnh minh họa.
   *
   * @param auction đối tượng auction cần hiển thị
   */
  private void updateDetailValues(Auction auction) {
    lblDetailName.setText(auction.getItem().getName());
    lblDetailStatus.setText("Trạng thái: " + calculateStatus(auction));
    lblDetailDesc.setText(auction.getItem().getDescription());
    
    String category = auction.getItem().getCategory() != null 
        ? auction.getItem().getCategory().name() : "DEFAULT";
    lblDetailCategory.setText(category);
    
    lblDetailCondition.setText(
        auction.getItem().getItemCondition() != null 
            ? auction.getItem().getItemCondition() : "--");
            
    lblDetailStartPrice.setText(String.format("%,.0f VNĐ", auction.getStartingPrice()));
    lblDetailEstPrice.setText(
        String.format("%,.0f VNĐ", auction.getItem().getEstimatedPrice()));
    
    if (auction.getSeller() != null) {
      lblDetailSeller.setText(auction.getSeller().getName() + " (" 
          + auction.getSeller().getEmail() + ")");
    } else {
      lblDetailSeller.setText("Đang cập nhật...");
    }

    // Tự động load ảnh Public Placeholder theo Danh mục
    try {
      String imageUrl;
      switch (category) {
        case "ART": 
          imageUrl = "https://images.unsplash.com/photo-1549887552-cb1071d3e5ca?w=200&h=200&fit=crop"; 
          break;
        case "ELECTRONICS": 
          imageUrl = "https://images.unsplash.com/photo-1498049794561-7780e7231661?w=200&h=200&fit=crop"; 
          break;
        case "VEHICLE": 
          imageUrl = "https://images.unsplash.com/photo-1502877338535-766e1452684a?w=200&h=200&fit=crop"; 
          break;
        case "JEWELRY": 
          imageUrl = "https://images.unsplash.com/photo-1599643478524-fb66f70200ad?w=200&h=200&fit=crop"; 
          break;
        case "COLLECTIBLES": 
          imageUrl = "https://images.unsplash.com/photo-1584826131336-db158cc10db3?w=200&h=200&fit=crop"; 
          break;
        default: 
          imageUrl = "https://images.unsplash.com/photo-1577717903315-1691ae25ab3f?w=200&h=200&fit=crop"; 
          break;
      }
      if (imgProduct != null) {
        // true: Load ảnh bất đồng bộ (background thread)
        imgProduct.setImage(new Image(imageUrl, true)); 
      }
    } catch (Exception e) {
      System.out.println("Bỏ qua lỗi nạp ảnh placeholder.");
    }
    
    if ("NOT OPEN".equals(calculateStatus(auction))) {
      btnJoinLive.setDisable(true);
      btnJoinLive.setText("PHIÊN CHƯA MỞ");
    } else {
      btnJoinLive.setDisable(false);
      btnJoinLive.setText("VÀO PHÒNG ĐẤU GIÁ TRỰC TIẾP ➔");
    }
  }

  @FXML
  private void handleJoinLiveBidding(ActionEvent event) {
    Auction selected = productTable.getSelectionModel().getSelectedItem();
    if (selected != null) {
      try {
        LiveBiddingController.targetAuctionId = selected.getAuctionId();
        App.setRoot("live_bidding");
      } catch (IOException e) {
        showAlert("LỖI HỆ THỐNG", "Không thể tải phòng Live Bidding.");
      }
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
          
          if (list != null) {
            auctionData.setAll(list);
            Auction selected = productTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
              Optional<Auction> updated = list.stream()
                  .filter(a -> a.getAuctionId().equals(selected.getAuctionId()))
                  .findFirst();
              updated.ifPresent(this::updateDetailValues);
            }
          }
        }
      } else if (ServerMessage.ACTION_EVENT.equals(response.getAction())) {
        refreshData();
      }
    });
  }

  private void startCountdownTimer() {
    countdownTimer = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
      if (productTable != null && !auctionData.isEmpty()) {
        productTable.refresh();
      }
    }));
    countdownTimer.setCycleCount(Animation.INDEFINITE);
    countdownTimer.play();
  }

  @FXML 
  private void handleRefresh(ActionEvent event) { 
    refreshData(); 
  }
  
  private void refreshData() {
    ClientMessage getAuctionsReq = ClientMessage.builder()
        .action(ActionType.GET_ALL_AUCTIONS)
        .build();
    NetworkClient.getInstance().sendMessage(getAuctionsReq);
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
  
  // --- GIỮ NGUYÊN CÁC HÀM CŨ ĐỂ KHÔNG MẤT TÍNH NĂNG ---

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
      showAlert("LỖI HỆ THỐNG", "Không thể ngắt kết nối."); 
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