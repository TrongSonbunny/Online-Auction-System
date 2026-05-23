package com.auction.controllers;

import com.auction.App;
import com.auction.models.auction.Auction;
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
import java.util.ResourceBundle;
import javafx.animation.Animation;
import javafx.animation.RotateTransition;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/** Điều khiển màn hình hiển thị danh sách đấu giá cho Bidder. */
public class PrimaryController implements Initializable, NetworkClient.MessageListener {

  @FXML
  private TableView<Auction> productTable;
  @FXML
  private TableColumn<Auction, String> colId;
  @FXML
  private TableColumn<Auction, String> colName;
  @FXML
  private TableColumn<Auction, Double> colPrice;
  @FXML
  private TextField txtBidAmount;

  private final ObservableList<Auction> auctionData = FXCollections.observableArrayList();

  private final Gson gson = new GsonBuilder()
      .registerTypeAdapter(
          LocalDateTime.class,
          (JsonDeserializer<LocalDateTime>) (json, type, ctx) -> {
            try {
              return LocalDateTime.parse(
                  json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (Exception e) {
              return LocalDateTime.now();
            }
          })
      .registerTypeAdapter(
          PermissionStrategy.class,
          (JsonDeserializer<PermissionStrategy>) (json, type, ctx) -> null)
      .registerTypeAdapter(
          PaymentStrategy.class, (JsonDeserializer<PaymentStrategy>) (json, type, ctx) -> null)
      .create();

  @Override
  public void initialize(URL url, ResourceBundle rb) {
    colId.setCellValueFactory(
        cellData -> {
          Auction auction = cellData.getValue();
          return new SimpleStringProperty(
              auction != null && auction.getAuctionId() != null ? auction.getAuctionId() : "");
        });

    colName.setCellValueFactory(
        cellData -> {
          Auction auction = cellData.getValue();
          String displayName = "";
          if (auction != null && auction.getItem() != null) {
            displayName = auction.getItem().getName();
          }
          return new SimpleStringProperty(displayName != null ? displayName : "");
        });

    colPrice.setCellValueFactory(
        cellData -> {
          Auction auction = cellData.getValue();
          return new SimpleObjectProperty<>(auction != null ? auction.getCurrentHighestBid() : 0.0);
        });

    productTable.setItems(auctionData);
    setupDynamicColorCoding();
    NetworkClient.getInstance().addListener(this);
    refreshData();
  }

  private void setupDynamicColorCoding() {
    productTable.setRowFactory(
        tv -> new TableRow<Auction>() {
          @Override
          protected void updateItem(Auction item, boolean empty) {
            super.updateItem(item, empty);
            getStyleClass()
                .removeAll("theme-electronics", "theme-art", "theme-vehicle", "hot-auction");

            if (item == null || empty) {
              setStyle("");
            } else {
              if (item.getItem() != null && item.getItem().getCategory() != null) {
                String category = item.getItem().getCategory().name();
                if ("ELECTRONICS".equals(category)) {
                  getStyleClass().add("theme-electronics");
                } else if ("ART".equals(category)) {
                  getStyleClass().add("theme-art");
                } else {
                  getStyleClass().add("theme-vehicle");
                }
              }
              if (item.getCurrentHighestBid() > 5000) {
                getStyleClass().add("hot-auction");
              }
            }
          }
        });
  }

  private void refreshData() {
    ClientMessage req = ClientMessage.builder().action(ActionType.GET_ALL_AUCTIONS).build();
    NetworkClient.getInstance().sendMessage(req);
  }

  @FXML
  private void handlePlaceBid(ActionEvent event) {
    Auction selected = productTable.getSelectionModel().getSelectedItem();
    if (selected == null) {
      showAlert("HỆ THỐNG", "Vui lòng chọn một vật phẩm trên radar để đặt giá!");
      return;
    }

    try {
      double amount = Double.parseDouble(txtBidAmount.getText());

      ClientMessage bidReq = ClientMessage.builder()
          .action(ActionType.BID)
          .userId(App.loggedInUserId)
          .email(App.loggedInEmail)
          .password(App.loggedInPassword)
          .role(UserRole.BIDDER)
          .auctionId(selected.getAuctionId())
          .bidAmount(amount)
          .build();

      NetworkClient.getInstance().sendMessage(bidReq);
    } catch (NumberFormatException e) {
      showAlert("CẢNH BÁO", "Dữ liệu nhập vào không phải là số tiền hợp lệ!");
    }
  }

  @Override
  public void onMessageReceived(ServerMessage response) {
    Platform.runLater(
        () -> {
          if (ActionType.GET_ALL_AUCTIONS.name().equals(response.getAction())) {

            Object dataSource = response.getData();
            if (dataSource == null) {
              dataSource = response.getAuctions();
            }

            if (dataSource != null) {
              String json = gson.toJson(dataSource);

              TypeToken<List<Auction>> token = new TypeToken<List<Auction>>() {
              };
              java.lang.reflect.Type type = token.getType();
              List<Auction> list = gson.fromJson(json, type);

              if (list != null) {
                auctionData.setAll(list);
              } else {
                auctionData.clear();
              }
            } else {
              auctionData.clear();
            }

          } else if (ServerMessage.ACTION_EVENT.equals(response.getAction())) {
            refreshData();
          } else if (ActionType.BID.name().equals(response.getAction())) {
            if (ServerMessage.STATUS_SUCCESS.equals(response.getStatus())) {

              fireConfettiGamification();
              txtBidAmount.clear();
              refreshData();

            } else {
              showAlert("TỪ CHỐI", response.getMessage());
            }
          }
        });
  }

  private void fireConfettiGamification() {
    Node rootNode = productTable.getScene().getRoot();
    if (rootNode instanceof StackPane) {
      StackPane root = (StackPane) rootNode;
      for (int i = 0; i < 50; i++) {
        Rectangle confetti = new Rectangle(6, 12);
        confetti.setFill(Color.hsb(Math.random() * 360, 0.8, 1.0));
        confetti.setTranslateX((Math.random() - 0.5) * 800);
        confetti.setTranslateY(-400);

        root.getChildren().add(confetti);

        Duration dur = Duration.seconds(1.5 + Math.random());
        TranslateTransition tt = new TranslateTransition(dur, confetti);
        tt.setByY(1000);
        tt.setByX((Math.random() - 0.5) * 200);

        RotateTransition rt = new RotateTransition(Duration.seconds(1), confetti);
        rt.setByAngle(360 * Math.random());
        rt.setCycleCount(Animation.INDEFINITE);

        tt.setOnFinished(e -> root.getChildren().remove(confetti));

        tt.play();
        rt.play();
      }
    }
  }

  @FXML
  private void handleLogout(ActionEvent event) {
    try {
      App.loggedInEmail = null;
      App.loggedInPassword = null;
      App.loggedInUserId = null;

      App.setRoot("login");
    } catch (IOException e) {
      showAlert("LỖI HỆ THỐNG", "Không thể ngắt kết nối trạm (Logout).");
    }
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
}