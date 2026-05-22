package com.auction.controllers;

import com.auction.App;
import com.auction.models.auction.Auction;
import com.auction.models.user.UserRole;
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
import java.util.List;
import java.util.ResourceBundle;
import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * Điều khiển màn hình hiển thị danh sách đấu giá cho Bidder.
 * Chứa cấu hình an toàn cho Gson và hiệu ứng gamification.
 */
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
          (JsonDeserializer<LocalDateTime>) (json, type, ctx) -> LocalDateTime.now())
      .create();

  @Override
  public void initialize(URL url, ResourceBundle rb) {
    colId.setCellValueFactory(new PropertyValueFactory<>("auctionId"));
    colName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
    colPrice.setCellValueFactory(new PropertyValueFactory<>("currentHighestBid"));

    productTable.setItems(auctionData);
    setupDynamicColorCoding();
    NetworkClient.getInstance().addListener(this);
    refreshData();
  }

  private void setupDynamicColorCoding() {
    productTable.setRowFactory(tv -> new TableRow<Auction>() {
      @Override
      protected void updateItem(Auction item, boolean empty) {
        super.updateItem(item, empty);
        getStyleClass().removeAll("theme-electronics", "theme-art", "theme-vehicle", "hot-auction");

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
    ClientMessage getAuctionsReq = ClientMessage.builder()
        .action(ActionType.GET_ALL_AUCTIONS)
        .build();
    NetworkClient.getInstance().sendMessage(getAuctionsReq);
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
          .email(App.loggedInEmail)
          .password(App.loggedInPassword)
          .role(UserRole.BIDDER) // ĐÃ FIX: Đính kèm Thẻ ngành để Server duyệt lệnh
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
    Platform.runLater(() -> {
      if (ActionType.GET_ALL_AUCTIONS.name().equals(response.getAction())) {
        String json = gson.toJson(response.getAuctions());
        List<Auction> list = gson.fromJson(json, new TypeToken<List<Auction>>() {
        }.getType());

        if (list != null) {
          auctionData.setAll(list);
        } else {
          auctionData.clear();
        }

      } else if (ServerMessage.ACTION_EVENT.equals(response.getAction())) {
        refreshData();
      } else if (ActionType.BID.name().equals(response.getAction())) {
        if (ServerMessage.STATUS_SUCCESS.equals(response.getStatus())) {
          fireConfettiGamification();
          txtBidAmount.clear();
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

        TranslateTransition tt = new TranslateTransition(
            Duration.seconds(1.5 + Math.random()), confetti);
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