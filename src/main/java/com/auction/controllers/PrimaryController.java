package com.auction.controllers;

import com.auction.models.Item;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

/**
 * Controller chính xử lý giao diện bảng danh sách (dành cho Bidder).
 * Tích hợp Socket kết nối Server để nhận dữ liệu Real-time.
 */
public class PrimaryController implements Initializable {

  @FXML
  private TableView<Item> productTable;

  @FXML
  private TableColumn<Item, String> colId;

  @FXML
  private TableColumn<Item, String> colName;

  @FXML
  private TableColumn<Item, Double> colPrice;

  @FXML
  private TextField txtBidAmount;

  private Socket socket;
  private ObjectOutputStream out;
  private ObjectInputStream in;
  private ObservableList<Item> tableData;
  private volatile boolean isRunning = true;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    colId.setCellValueFactory(new PropertyValueFactory<>("id"));
    colName.setCellValueFactory(new PropertyValueFactory<>("name"));
    colPrice.setCellValueFactory(new PropertyValueFactory<>("startingPrice"));

    tableData = FXCollections.observableArrayList();
    productTable.setItems(tableData);

    connectToServer();
  }

  /**
   * Kết nối tới Server và liên tục lắng nghe dữ liệu cập nhật.
   */
  private void connectToServer() {
    new Thread(() -> {
      try {
        socket = new Socket("127.0.0.1", 8080);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());

        while (isRunning) {
          Object response = in.readObject();

          if (response instanceof List) {
            @SuppressWarnings("unchecked")
            List<Item> initialItems = (List<Item>) response;
            Platform.runLater(() -> {
              tableData.setAll(initialItems);
            });
          } else if (response instanceof Item) {
            Item updatedItem = (Item) response;
            Platform.runLater(() -> {
              for (Item item : tableData) {
                if (item.getId().equals(updatedItem.getId())) {
                  item.setStartingPrice(updatedItem.getStartingPrice());
                  break;
                }
              }
              productTable.refresh();
              showAlert(AlertType.INFORMATION, "Cập nhật từ Server",
                  "Sản phẩm '" + updatedItem.getName() + "' vừa được đặt giá mới: $"
                      + updatedItem.getStartingPrice());
            });
          }
        }
      } catch (IOException | ClassNotFoundException e) {
        if (isRunning) {
          System.err.println("Mất kết nối tới Server: " + e.getMessage());
        }
      } finally {
        closeResources(); // Đảm bảo dọn dẹp bộ nhớ khi luồng kết thúc
      }
    }).start();
  }

  /**
   * Xử lý sự kiện đặt giá, gửi yêu cầu qua Server.
   */
  @FXML
  private void handlePlaceBid() {
    Item selectedItem = productTable.getSelectionModel().getSelectedItem();
    if (selectedItem == null) {
      showAlert(AlertType.WARNING, "Cảnh báo", "Vui lòng chọn một sản phẩm để đặt giá!");
      return;
    }

    String bidText = txtBidAmount.getText();
    if (bidText == null || bidText.trim().isEmpty()) {
      showAlert(AlertType.WARNING, "Cảnh báo", "Vui lòng nhập số tiền muốn đặt!");
      return;
    }

    try {
      double bidAmount = Double.parseDouble(bidText);

      if (bidAmount <= selectedItem.getStartingPrice()) {
        showAlert(AlertType.ERROR, "Lỗi đặt giá", "Giá đặt phải lớn hơn giá hiện tại ($"
            + selectedItem.getStartingPrice() + ")!");
        return;
      }

      selectedItem.setStartingPrice(bidAmount);

      if (out != null) {
        out.reset();
        out.writeObject(selectedItem);
        out.flush();
        txtBidAmount.clear();
      } else {
        showAlert(AlertType.ERROR, "Lỗi mạng", "Chưa kết nối được với Server!");
      }

    } catch (NumberFormatException e) {
      showAlert(AlertType.ERROR, "Lỗi dữ liệu", "Số tiền không hợp lệ. Vui lòng chỉ nhập số!");
    } catch (IOException e) {
      showAlert(AlertType.ERROR, "Lỗi mạng", "Không thể gửi dữ liệu lên Server!");
    }
  }

  /**
   * Xử lý sự kiện đăng xuất: Ngắt kết nối mạng và quay lại màn hình Login.
   *
   * @param event Sự kiện click nút Đăng xuất
   */
  @FXML
  private void handleLogout(ActionEvent event) {
    isRunning = false; // Ngừng vòng lặp lắng nghe
    closeResources(); // Đóng kết nối lập tức để Server nhận diện và xóa Client

    try {
      Parent loginRoot = FXMLLoader.load(getClass().getResource("/com/auction/login.fxml"));
      Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

      // Tạo Scene mới với kích thước cố định để tránh bị thu nhỏ cửa sổ
      Scene loginScene = new Scene(loginRoot, 800, 600);

      stage.setScene(loginScene);
      stage.setTitle("Đăng nhập - Đấu giá trực tuyến");
      stage.centerOnScreen(); // Tự động căn giữa màn hình
      stage.show();
    } catch (IOException e) {
      showAlert(AlertType.ERROR, "Lỗi hệ thống", "Không thể tải màn hình đăng nhập!");
      System.err.println("Lỗi load login.fxml: " + e.getMessage());
    }
  }

  /**
   * Phương thức phụ trợ đóng tất cả tài nguyên mạng an toàn.
   */
  private void closeResources() {
    try {
      if (socket != null && !socket.isClosed()) {
        socket.close(); // Đóng socket trước để ép luồng readObject() ném Exception và thoát ngay
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

  /**
   * Hiển thị hộp thoại thông báo cho người dùng.
   *
   * @param type    Loại cảnh báo (INFO, WARNING, ERROR)
   * @param title   Tiêu đề hộp thoại
   * @param content Nội dung chi tiết
   */
  private void showAlert(AlertType type, String title, String content) {
    Alert alert = new Alert(type);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(content);
    alert.showAndWait();
  }
}