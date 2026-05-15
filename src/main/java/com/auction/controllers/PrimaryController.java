package com.auction.controllers;

import com.auction.App;
import com.auction.models.auction.Auction;
import com.auction.network.ActionType;
import com.auction.network.ClientMessage;
import com.auction.network.NetworkClient;
import com.auction.network.ServerMessage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Controller xử lý màn hình chính dành cho người tham gia đấu giá (Bidder).
 * Có tích hợp cập nhật dữ liệu thời gian thực.
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
  private final Gson gson = new Gson();

  /**
   * Khởi tạo bảng dữ liệu và yêu cầu lấy danh sách đấu giá từ máy chủ.
   *
   * @param url định vị tài nguyên (mặc định của JavaFX)
   * @param rb  gói tài nguyên ngôn ngữ (mặc định của JavaFX)
   */
  @Override
  public void initialize(URL url, ResourceBundle rb) {
    colId.setCellValueFactory(new PropertyValueFactory<>("auctionId"));
    colName.setCellValueFactory(new PropertyValueFactory<>("item"));
    colPrice.setCellValueFactory(new PropertyValueFactory<>("currentPrice"));

    productTable.setItems(auctionData);

    NetworkClient.getInstance().addListener(this);
    refreshData();
  }

  /**
   * Gửi lệnh GET_ALL_AUCTIONS để làm mới danh sách.
   */
  private void refreshData() {
    ClientMessage request = ClientMessage.builder()
        .action(ActionType.GET_ALL_AUCTIONS)
        .build();
    NetworkClient.getInstance().sendMessage(request);
  }

  /**
   * Xử lý sự kiện đặt giá khi người dùng nhấn nút Đấu giá.
   */
  @FXML
  private void handleBid() {
    Auction selected = productTable.getSelectionModel().getSelectedItem();
    if (selected == null) {
      showAlert("Vui lòng chọn một phiên đấu giá để tham gia!");
      return;
    }

    try {
      double amount = Double.parseDouble(txtBidAmount.getText());
      ClientMessage bidMsg = ClientMessage.builder()
          .action(ActionType.BID)
          .auctionId(selected.getAuctionId())
          .bidAmount(amount)
          .userId("CURRENT_USER") // Backend sẽ cần ID user thực tế
          .build();

      NetworkClient.getInstance().sendMessage(bidMsg);
      txtBidAmount.clear();
    } catch (NumberFormatException e) {
      showAlert("Số tiền đấu giá không hợp lệ!");
    }
  }

  /**
   * Lắng nghe phản hồi từ máy chủ và cập nhật TableView.
   *
   * @param response dữ liệu trả về từ máy chủ
   */
  @Override
  public void onMessageReceived(ServerMessage response) {
    Platform.runLater(() -> {
      if (ActionType.GET_ALL_AUCTIONS.name().equals(response.getAction())) {
        String json = gson.toJson(response.getAuctions());
        List<Auction> list = gson.fromJson(json, new TypeToken<List<Auction>>() {
        }.getType());
        auctionData.setAll(list);
      } else if (ServerMessage.ACTION_EVENT.equals(response.getAction())) {
        // Có người vừa trả giá hoặc thêm sản phẩm -> Cập nhật lại toàn bộ
        refreshData();
      } else if (ActionType.BID.name().equals(response.getAction())
          && ServerMessage.STATUS_ERROR.equals(response.getStatus())) {
        showAlert(response.getMessage());
      }
    });
  }

  /**
   * Xử lý đăng xuất và quay lại màn hình Login.
   *
   * @param event sự kiện click chuột
   */
  @FXML
  private void handleLogout(ActionEvent event) {
    try {
      App.setRoot("login");
    } catch (IOException e) {
      showAlert("Không thể quay lại màn hình đăng nhập.");
    }
  }

  private void showAlert(String content) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("Thông báo");
    alert.setHeaderText(null);
    alert.setContentText(content);
    alert.showAndWait();
  }
}