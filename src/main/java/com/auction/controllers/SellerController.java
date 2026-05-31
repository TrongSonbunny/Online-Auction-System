package com.auction.controllers;

import com.auction.App;
import com.auction.models.auction.Auction;
import com.auction.models.item.ItemCategory;
import com.auction.models.payment.PaymentStrategy;
import com.auction.models.user.UserRole;
import com.auction.models.user.permission.PermissionStrategy;
import com.auction.network.ActionType;
import com.auction.network.ClientMessage;
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
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Điều khiển màn hình của Seller (Người bán) để tạo và quản lý các phiên đấu giá.
 */
public class SellerController implements Initializable, NetworkClient.MessageListener {

  @FXML private HBox titleBar;
  @FXML private Label lblStatus;
  @FXML private TableView<Auction> auctionTable;
  @FXML private TableColumn<Auction, String> colId;
  @FXML private TableColumn<Auction, String> colName;
  @FXML private TableColumn<Auction, Double> colCurrentPrice;
  @FXML private TableColumn<Auction, String> colStartTime;
  @FXML private TableColumn<Auction, String> colEndTime;
  @FXML private TableColumn<Auction, String> colDuration;
  @FXML private TableColumn<Auction, String> colTimeRemaining;
  @FXML private TableColumn<Auction, String> colStatus;

  @FXML private TextField txtName;
  @FXML private TextArea txtDescription;
  @FXML private ComboBox<ItemCategory> cbCategory;
  @FXML private TextField txtCondition;
  @FXML private TextField txtEstimatedPrice;
  @FXML private TextField txtStartingPrice;
  @FXML private TextField txtDuration;
  @FXML private Button btnSubmit;

  private final ObservableList<Auction> myAuctions = FXCollections.observableArrayList();
  private AnimationTimer meshGradientTimer;
  private Timeline countdownTimer;
  
  private double gradientOffset = 0.0;
  private double offsetX = 0;
  private double offsetY = 0;
  private long lastAutoRefreshTime = 0; // ĐÃ THÊM: Chống Spam Request
  
  private boolean isEditMode = false;
  private Auction selectedAuctionForEdit = null;

  // Gson bỏ qua việc khởi tạo PaymentStrategy
  private final Gson gson = new GsonBuilder()
      .registerTypeAdapter(LocalDateTime.class,
          (JsonDeserializer<LocalDateTime>) (json, type, ctx) ->
              LocalDateTime.parse(
                  json.getAsString(), 
                  DateTimeFormatter.ISO_LOCAL_DATE_TIME))
      .registerTypeAdapter(PermissionStrategy.class,
          (JsonDeserializer<PermissionStrategy>) (json, type, ctx) -> null)
      .registerTypeAdapter(PaymentStrategy.class,
          (JsonDeserializer<PaymentStrategy>) (json, type, ctx) -> null)
      .create();

  @Override
  public void initialize(URL url, ResourceBundle rb) {
    NetworkClient.getInstance().addListener(this);
    setupUndecoratedWindowHandle();
    
    if (cbCategory != null) {
      cbCategory.setItems(FXCollections.observableArrayList(ItemCategory.values()));
    }
    
    setupTableColumns();
    startCountdownTimer();

    Platform.runLater(() -> {
      if (txtName != null && txtName.getScene() != null) {
        Node root = txtName.getScene().getRoot();
        if (root != null) {
          startMeshGradientAnimation(root);
        }
      }
      requestAuctionsData();
    });
  }

  private void setupTableColumns() {
    final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM");

    colId.setCellValueFactory(d -> new SimpleStringProperty(
        d.getValue() != null ? d.getValue().getAuctionId() : ""));
        
    colName.setCellValueFactory(d -> new SimpleStringProperty(
        d.getValue() != null && d.getValue().getItem() != null
            ? d.getValue().getItem().getName() : "Unknown"));
            
    colCurrentPrice.setCellValueFactory(d -> new SimpleObjectProperty<>(
        d.getValue() != null ? d.getValue().getCurrentHighestBid() : 0.0));
    
    colStartTime.setCellValueFactory(d -> {
      Auction a = d.getValue();
      if (a == null || a.getStartTime() == null) {
        return new SimpleStringProperty("--");
      }
      return new SimpleStringProperty(a.getStartTime().format(dtf));
    });
    
    colEndTime.setCellValueFactory(d -> {
      Auction a = d.getValue();
      LocalDateTime end = a != null ? a.getScheduledEndTime() : null;
      if (end == null) {
        return new SimpleStringProperty("--");
      }
      return new SimpleStringProperty(end.format(dtf));
    });
    
    colDuration.setCellValueFactory(d -> {
      Auction a = d.getValue();
      if (a == null) {
        return new SimpleStringProperty("--");
      }
      return new SimpleStringProperty(a.getDurationSeconds() + "s");
    });
    
    colTimeRemaining.setCellValueFactory(d -> {
      Auction a = d.getValue();
      if (a == null) {
        return new SimpleStringProperty("--");
      }
      String status = a.getStatus() != null ? a.getStatus().name() : "";
      if ("PENDING".equals(status)) {
        return new SimpleStringProperty("Chưa mở");
      }
      
      LocalDateTime end = a.getScheduledEndTime();
      if (!"ACTIVE".equals(status) || end == null) {
        return new SimpleStringProperty("00:00:00");
      }
      
      LocalDateTime now = LocalDateTime.now();
      if (now.isAfter(end)) {
        return new SimpleStringProperty("00:00:00");
      }
      long s = java.time.Duration.between(now, end).getSeconds();
      return new SimpleStringProperty(
          String.format("%02d:%02d:%02d", s / 3600, (s % 3600) / 60, (s % 60)));
    });
    
    colStatus.setCellValueFactory(d -> {
      Auction a = d.getValue();
      if (a == null || a.getStatus() == null) {
        return new SimpleStringProperty("");
      }
      String rawStatus = a.getStatus().name();
      if ("PENDING".equals(rawStatus)) {
        return new SimpleStringProperty("NOT OPEN");
      }
      if ("ACTIVE".equals(rawStatus)) {
        LocalDateTime end = a.getScheduledEndTime();
        if (end != null && LocalDateTime.now().isAfter(end)) {
          return new SimpleStringProperty("CLOSED");
        }
        return new SimpleStringProperty("OPEN");
      }
      return new SimpleStringProperty("CLOSED");
    });
    
    if (auctionTable != null) {
      auctionTable.setItems(myAuctions);
    }
  }

  private void startCountdownTimer() {
    countdownTimer = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
      boolean shouldRefresh = false;
      if (auctionTable != null && !myAuctions.isEmpty()) {
        auctionTable.refresh();
        // ĐÃ THÊM: Quét xem có phiên nào vừa hết giờ không để Auto-fetch người thắng!
        for (Auction a : myAuctions) {
          if ("ACTIVE".equals(a.getStatus().name()) && a.getScheduledEndTime() != null) {
            if (LocalDateTime.now().isAfter(a.getScheduledEndTime())) {
              shouldRefresh = true;
              break;
            }
          }
        }
      }

      // Delay 3s giữa các lần fetch tự động để tránh spam Server
      if (shouldRefresh && System.currentTimeMillis() - lastAutoRefreshTime > 3000) {
        lastAutoRefreshTime = System.currentTimeMillis();
        requestAuctionsData();
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
        targetNode.setStyle(
            String.format(Locale.US,
                "-fx-background-color: linear-gradient(to bottom right, #0a0a0a, "
                    + "rgba(26, 21, 5, %f), rgba(5, 5, 5, %f));",
                (Math.sin(gradientOffset) * 0.1 + 0.9),
                (Math.cos(gradientOffset) * 0.1 + 0.9)));
      }
    };
    meshGradientTimer.start();
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

  /**
   * Kiểm tra tính hợp lệ của form bằng hình ảnh trực quan.
   *
   * @return true nếu hợp lệ
   */
  private boolean validateInputs() {
    boolean isValid = true;

    // Reset styles
    if (txtName != null) {
      txtName.setStyle("");
    }
    if (txtDescription != null) {
      txtDescription.setStyle("");
    }
    if (cbCategory != null) {
      cbCategory.setStyle("");
    }
    if (txtCondition != null) {
      txtCondition.setStyle("");
    }
    if (txtEstimatedPrice != null) {
      txtEstimatedPrice.setStyle("");
    }
    if (txtStartingPrice != null) {
      txtStartingPrice.setStyle("");
    }
    if (txtDuration != null) {
      txtDuration.setStyle("");
    }

    if (txtName != null && txtName.getText().trim().isEmpty()) {
      txtName.setStyle("-fx-border-color: #ff4444; -fx-border-width: 2;");
      triggerShakeAnimation(txtName);
      isValid = false;
    }
    if (txtDescription != null && txtDescription.getText().trim().isEmpty()) {
      txtDescription.setStyle("-fx-border-color: #ff4444; -fx-border-width: 2;");
      triggerShakeAnimation(txtDescription);
      isValid = false;
    }
    if (cbCategory != null && cbCategory.getValue() == null) {
      cbCategory.setStyle("-fx-border-color: #ff4444; -fx-border-width: 2;");
      triggerShakeAnimation(cbCategory);
      isValid = false;
    }
    if (txtCondition != null && txtCondition.getText().trim().isEmpty()) {
      txtCondition.setStyle("-fx-border-color: #ff4444; -fx-border-width: 2;");
      triggerShakeAnimation(txtCondition);
      isValid = false;
    }

    if (txtEstimatedPrice != null) {
      if (txtEstimatedPrice.getText().trim().isEmpty()) {
        txtEstimatedPrice.setStyle("-fx-border-color: #ff4444; -fx-border-width: 2;");
        triggerShakeAnimation(txtEstimatedPrice);
        isValid = false;
      } else {
        try {
          Double.parseDouble(txtEstimatedPrice.getText());
        } catch (NumberFormatException e) {
          txtEstimatedPrice.setStyle("-fx-border-color: #ff4444; -fx-border-width: 2;");
          triggerShakeAnimation(txtEstimatedPrice);
          isValid = false;
        }
      }
    }

    if (txtStartingPrice != null) {
      if (txtStartingPrice.getText().trim().isEmpty()) {
        txtStartingPrice.setStyle("-fx-border-color: #ff4444; -fx-border-width: 2;");
        triggerShakeAnimation(txtStartingPrice);
        isValid = false;
      } else {
        try {
          Double.parseDouble(txtStartingPrice.getText());
        } catch (NumberFormatException e) {
          txtStartingPrice.setStyle("-fx-border-color: #ff4444; -fx-border-width: 2;");
          triggerShakeAnimation(txtStartingPrice);
          isValid = false;
        }
      }
    }

    if (txtDuration != null) {
      if (txtDuration.getText().trim().isEmpty()) {
        txtDuration.setStyle("-fx-border-color: #ff4444; -fx-border-width: 2;");
        triggerShakeAnimation(txtDuration);
        isValid = false;
      } else {
        try {
          Long.parseLong(txtDuration.getText());
        } catch (NumberFormatException e) {
          txtDuration.setStyle("-fx-border-color: #ff4444; -fx-border-width: 2;");
          triggerShakeAnimation(txtDuration);
          isValid = false;
        }
      }
    }

    return isValid;
  }

  @FXML
  private void handleCreateOrUpdateAuction(ActionEvent event) {
    if (!validateInputs()) {
      setStatus("Vui lòng điền đúng và đủ thông tin bắt buộc (viền đỏ)!", true);
      return;
    }

    try {
      ClientMessage.Builder builder = ClientMessage.builder()
          .userId(App.loggedInUserId)
          .email(App.loggedInEmail)
          .password(App.loggedInPassword)
          .role(UserRole.SELLER)
          .itemName(txtName.getText().trim())
          .itemCategory(cbCategory.getValue().name())
          .itemDescription(txtDescription.getText().trim())
          .itemCondition(txtCondition.getText().trim())
          .estimatedPrice(Double.parseDouble(txtEstimatedPrice.getText()))
          .startingPrice(Double.parseDouble(txtStartingPrice.getText()))
          .durationSeconds(Long.parseLong(txtDuration.getText()));

      if (isEditMode && selectedAuctionForEdit != null) {
        builder.action(ActionType.UPDATE_AUCTION);
        builder.auctionId(selectedAuctionForEdit.getAuctionId());
      } else {
        builder.action(ActionType.CREATE_AUCTION);
      }

      NetworkClient.getInstance().sendMessage(builder.build());
    } catch (NumberFormatException e) {
      setStatus("CẢNH BÁO: Định dạng giá tiền hoặc thời lượng không hợp lệ!", true);
    }
  }

  @FXML
  private void handleEditAuction(ActionEvent event) {
    Auction selected = auctionTable.getSelectionModel().getSelectedItem();
    if (selected == null) {
      setStatus("Vui lòng chọn sản phẩm cần sửa trên bảng!", true);
      return;
    }
    if (!"PENDING".equals(selected.getStatus().name())) {
      setStatus("Chỉ có thể sửa thông tin khi sản phẩm chưa mở đấu giá (NOT OPEN)!", true);
      return;
    }
    
    isEditMode = true;
    selectedAuctionForEdit = selected;
    
    txtName.setText(selected.getItem().getName());
    txtDescription.setText(selected.getItem().getDescription());
    cbCategory.setValue(ItemCategory.valueOf(selected.getItem().getCategory().name()));
    txtCondition.setText(selected.getItem().getItemCondition());
    txtEstimatedPrice.setText(String.valueOf(selected.getItem().getEstimatedPrice()));
    txtStartingPrice.setText(String.valueOf(selected.getStartingPrice()));
    txtDuration.setText(String.valueOf(selected.getDurationSeconds()));
    
    btnSubmit.setText("LƯU CẬP NHẬT SẢN PHẨM");
    setStatus("Đang trong chế độ CHỈNH SỬA sản phẩm.", false);
  }

  @FXML
  private void handleStartAuction(ActionEvent event) {
    Auction selected = auctionTable.getSelectionModel().getSelectedItem();
    if (selected == null) {
      setStatus("Vui lòng chọn sản phẩm trên bảng để bắt đầu!", true);
      return;
    }
    if (!"PENDING".equals(selected.getStatus().name())) {
      setStatus("Sản phẩm này đã được mở hoặc đã kết thúc!", true);
      return;
    }
    
    ClientMessage startReq = ClientMessage.builder()
        .action(ActionType.START_AUCTION)
        .userId(App.loggedInUserId)
        .auctionId(selected.getAuctionId())
        .build();
    NetworkClient.getInstance().sendMessage(startReq);
  }

  @FXML
  private void handleCancelAuction(ActionEvent event) {
    if (auctionTable == null || auctionTable.getSelectionModel().getSelectedItem() == null) {
      setStatus("Vui lòng chọn một phiên trên bảng để hủy/xóa!", true);
      return;
    }
    
    Auction selected = auctionTable.getSelectionModel().getSelectedItem();
    String status = selected.getStatus().name();
    
    if ("ACTIVE".equals(status)) {
      if (selected.getScheduledEndTime() != null 
          && LocalDateTime.now().isBefore(selected.getScheduledEndTime())) {
        setStatus("Không thể xóa sản phẩm đang trong quá trình đấu giá (OPEN)!", true);
        return;
      }
    }
    
    ClientMessage cancelReq = ClientMessage.builder()
        .action(ActionType.CANCEL_AUCTION)
        .userId(App.loggedInUserId)
        .auctionId(selected.getAuctionId())
        .build();
    NetworkClient.getInstance().sendMessage(cancelReq);
  }

  @FXML
  private void handleRefresh(ActionEvent event) {
    setStatus("Đang làm mới danh sách đấu giá...", false);
    requestAuctionsData();
  }
  
  private void requestAuctionsData() {
    ClientMessage getReq = ClientMessage.builder()
        .action(ActionType.GET_ALL_AUCTIONS)
        .build();
    NetworkClient.getInstance().sendMessage(getReq);
  }

  @FXML
  private void handleClearForm(ActionEvent event) {
    isEditMode = false;
    selectedAuctionForEdit = null;
    if (btnSubmit != null) {
      btnSubmit.setText("LƯU DỮ LIỆU (NOT OPEN)");
    }
    clearInputs();
    
    // Đặt lại style nếu trước đó đang báo lỗi
    if (txtName != null) {
      txtName.setStyle("");
    }
    if (txtDescription != null) {
      txtDescription.setStyle("");
    }
    if (cbCategory != null) {
      cbCategory.setStyle("");
    }
    if (txtCondition != null) {
      txtCondition.setStyle("");
    }
    if (txtEstimatedPrice != null) {
      txtEstimatedPrice.setStyle("");
    }
    if (txtStartingPrice != null) {
      txtStartingPrice.setStyle("");
    }
    if (txtDuration != null) {
      txtDuration.setStyle("");
    }

    setStatus("Đã làm mới form niêm yết.", false);
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
            myAuctions.setAll(list.stream()
                .filter(a -> a.getSeller() != null
                    && a.getSeller().getUserId().equals(App.loggedInUserId))
                .toList());
          }
        }
      } else if (ActionType.CREATE_AUCTION.name().equals(response.getAction())
          || ActionType.UPDATE_AUCTION.name().equals(response.getAction())
          || ActionType.START_AUCTION.name().equals(response.getAction())
          || ActionType.CANCEL_AUCTION.name().equals(response.getAction())) {
          
        if (ServerMessage.STATUS_SUCCESS.equals(response.getStatus())) {
          setStatus("THÀNH CÔNG! Đã cập nhật trạng thái phiên đấu giá.", false);
          if (ActionType.CREATE_AUCTION.name().equals(response.getAction())
              || ActionType.UPDATE_AUCTION.name().equals(response.getAction())) {
            handleClearForm(null);
          }
          requestAuctionsData();
        } else {
          setStatus("LỖI: " + response.getMessage(), true);
        }
      } else if (ServerMessage.ACTION_EVENT.equals(response.getAction())) {
        requestAuctionsData();
      }
      
    });
  }

  private void setStatus(String msg, boolean isError) {
    if (lblStatus != null) {
      lblStatus.setStyle(isError ? "-fx-text-fill: #ff4444;" : "-fx-text-fill: #d4af37;");
      lblStatus.setText(msg);
    }
  }

  private void clearInputs() {
    if (txtName != null) {
      txtName.clear();
    }
    if (txtDescription != null) {
      txtDescription.clear();
    }
    if (cbCategory != null) {
      cbCategory.getSelectionModel().clearSelection();
    }
    if (txtCondition != null) {
      txtCondition.clear();
    }
    if (txtEstimatedPrice != null) {
      txtEstimatedPrice.clear();
    }
    if (txtStartingPrice != null) {
      txtStartingPrice.clear();
    }
    if (txtDuration != null) {
      txtDuration.clear();
    }
  }

  @FXML
  private void handleLogout(ActionEvent event) {
    try {
      App.loggedInEmail = null;
      App.loggedInPassword = null;
      App.loggedInUserId = null;
      
      if (meshGradientTimer != null) {
        meshGradientTimer.stop();
      }
      if (countdownTimer != null) {
        countdownTimer.stop();
      }
      
      App.setRoot("login");
    } catch (IOException e) {
      setStatus("Lỗi hệ thống khi thực hiện đăng xuất.", true);
    }
  }

  @FXML
  private void handleMinimize(ActionEvent event) {
    ((Stage) ((Node) event.getSource()).getScene().getWindow()).setIconified(true);
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
    ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
  }

  private void setupUndecoratedWindowHandle() {
    if (titleBar != null) {
      titleBar.setOnMousePressed(
          event -> {
            offsetX = event.getSceneX();
            offsetY = event.getSceneY();
          });
      titleBar.setOnMouseDragged(
          event -> {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setX(event.getScreenX() - offsetX);
            stage.setY(event.getScreenY() - offsetY);
          });
    }
  }
}