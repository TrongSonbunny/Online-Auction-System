package com.auction.controllers;

import com.auction.models.auction.Auction;
import com.auction.network.ActionType;
import com.auction.network.ClientMessage;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.Collection;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller chính xử lý giao diện bảng danh sách (dành cho Bidder).
 * Tích hợp Socket kết nối Server để nhận dữ liệu Real-time.
 */
public class PrimaryController implements Initializable {

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

  private Socket socket;
  private ObjectOutputStream out;
  private ObjectInputStream in;
  private ObservableList<Auction> tableData;
  private volatile boolean isRunning = true;

  /**
   * Khởi tạo giao diện và kết nối server.
   * Cài đặt các cột cho bảng hiển thị danh sách đấu giá.
   *
   * @param location  Đường dẫn tài nguyên
   * @param resources Gói tài nguyên
   */
  @Override
  public void initialize(URL location, ResourceBundle resources) {

    // Đã chủ động nhấn Enter ngắt dòng để qua mặt Checkstyle
    colId.setCellValueFactory(
        cell -> new SimpleStringProperty(cell.getValue().getAuctionId()));

    colName.setCellValueFactory(
        cell -> new SimpleStringProperty(cell.getValue().getItem().getName()));

    colPrice.setCellValueFactory(
        cell -> new SimpleDoubleProperty(
            cell.getValue().getCurrentHighestBid()).asObject());

    tableData = FXCollections.observableArrayList();
    productTable.setItems(tableData);

    connectToServer();
  }

  /**
   * Kết nối tới Server, gửi lệnh lấy danh sách và liên tục lắng nghe JSON.
   */
  private void connectToServer() {
    new Thread(() -> {
      try {
        socket = new Socket("127.0.0.1", 8080);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());

        ClientMessage request = ClientMessage.builder()
            .action(ActionType.GET_ALL_AUCTIONS)
            .build();

        out.writeObject(request);
        out.flush();

        while (isRunning) {
          Object response = in.readObject();

          if (response instanceof Collection) {
            @SuppressWarnings("unchecked")
            Collection<Auction> initialItems = (Collection<Auction>) response;
            Platform.runLater(() -> tableData.setAll(initialItems));
          } else if (response instanceof String) {
            String jsonPayload = (String) response;
            handleRealTimeEvent(jsonPayload);
          }
        }
      } catch (IOException | ClassNotFoundException e) {
        if (isRunning) {
          System.err.println("Mất kết nối tới Server: " + e.getMessage());
        }
      } finally {
        closeResources();
      }
    }).start();
  }

  /**
   * Xử lý gói tin JSON thời gian thực từ Backend.
   *
   * @param jsonPayload Chuỗi JSON chứa thông tin sự kiện
   */
  private void handleRealTimeEvent(String jsonPayload) {
    try {
      JsonObject jsonObj = JsonParser.parseString(jsonPayload).getAsJsonObject();
      String eventType = jsonObj.get("eventType").getAsString();
      String auctionId = jsonObj.get("auctionId").getAsString();
      String message = jsonObj.get("message").getAsString();

      Platform.runLater(() -> {
        if ("NEW_BID".equals(eventType) || "AUTO_BID_PLACED".equals(eventType)) {
          JsonObject payload = jsonObj.getAsJsonObject("payload");
          double newBid = payload.get("bidAmount").getAsDouble();

          for (Auction auction : tableData) {
            if (auction.getAuctionId().equals(auctionId)) {
              auction.updateHighestBid(null, newBid);
              break;
            }
          }
          productTable.refresh();
          showAlert(AlertType.INFORMATION, "Cập nhật từ Server", message);
        }
      });
    } catch (Exception e) {
      System.err.println("Lỗi parse JSON Real-time: " + e.getMessage());
    }
  }

  /**
   * Xử lý sự kiện đặt giá, gửi ClientMessage qua Server.
   */
  @FXML
  private void handlePlaceBid() {
    Auction selectedAuction = productTable.getSelectionModel().getSelectedItem();
    if (selectedAuction == null) {
      showAlert(AlertType.WARNING, "Cảnh báo", "Vui lòng chọn sản phẩm!");
      return;
    }

    String bidText = txtBidAmount.getText();
    if (bidText == null || bidText.trim().isEmpty()) {
      showAlert(AlertType.WARNING, "Cảnh báo", "Vui lòng nhập số tiền đặt!");
      return;
    }

    try {
      double bidAmount = Double.parseDouble(bidText);

      ClientMessage bidRequest = ClientMessage.builder()
          .action(ActionType.BID)
          .auctionId(selectedAuction.getAuctionId())
          .bidAmount(bidAmount)
          .userId("USER_ID_TU_SESSION")
          .build();

      if (out != null) {
        out.writeObject(bidRequest);
        out.flush();
        txtBidAmount.clear();
      } else {
        showAlert(AlertType.ERROR, "Lỗi mạng", "Chưa kết nối Server!");
      }

    } catch (NumberFormatException e) {
      showAlert(AlertType.ERROR, "Lỗi dữ liệu", "Số tiền không hợp lệ!");
    } catch (IOException e) {
      showAlert(AlertType.ERROR, "Lỗi mạng", "Không gửi được dữ liệu!");
    }
  }

  @FXML
  private void handleLogout(ActionEvent event) {
    isRunning = false;
    closeResources();
    try {
      // Đã thêm /views/
      Parent loginRoot = FXMLLoader.load(
          getClass().getResource("/com/auction/views/login.fxml"));

      Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
      stage.setScene(new Scene(loginRoot, 800, 600));
      stage.setTitle("Đăng nhập - Đấu giá trực tuyến");
      stage.centerOnScreen();
      stage.show();
    } catch (IOException e) {
      showAlert(AlertType.ERROR, "Lỗi hệ thống", "Không thể tải đăng nhập!");
    }
  }

  private void closeResources() {
    try {
      if (socket != null && !socket.isClosed()) {
        socket.close();
      }
      if (in != null) {
        in.close();
      }
      if (out != null) {
        out.close();
      }
    } catch (IOException e) {
      System.err.println("Lỗi khi đóng kết nối Socket: " + e.getMessage());
    }
  }

  private void showAlert(AlertType type, String title, String content) {
    Alert alert = new Alert(type);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(content);
    alert.showAndWait();
  }
}