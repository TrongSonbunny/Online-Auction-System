package com.auction.controllers;

import com.auction.models.Message;
import com.auction.models.item.AuctionItem;
import com.auction.network.AuctionClient;
import java.io.IOException;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * Controller xử lý logic cho màn hình quản lý sản phẩm của Người bán (Seller).
 * Đã tích hợp giao diện Dark Mode (kéo thả) và gửi luồng JSON.
 */
public class SellerController {

  @FXML
  private HBox titleBar;

  @FXML
  private TextField txtName;

  @FXML
  private TextField txtCategory;

  @FXML
  private TextField txtPrice;

  private double offsetX = 0.0;
  private double offsetY = 0.0;

  /**
   * Phương thức chạy khi giao diện được tải.
   * Thiết lập tính năng kéo thả cửa sổ.
   */
  @FXML
  public void initialize() {
    if (titleBar != null) {
      titleBar.setOnMousePressed(event -> {
        offsetX = event.getSceneX();
        offsetY = event.getSceneY();
      });

      titleBar.setOnMouseDragged(event -> {
        Stage stage = (Stage) titleBar.getScene().getWindow();
        if (!stage.isMaximized()) {
          stage.setX(event.getScreenX() - offsetX);
          stage.setY(event.getScreenY() - offsetY);
        }
      });
    }
  }

  @FXML
  private void handleMinimize(ActionEvent event) {
    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    stage.setIconified(true);
  }

  @FXML
  private void handleMaximize(ActionEvent event) {
    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    stage.setMaximized(!stage.isMaximized());
  }

  @FXML
  private void handleClose() {
    Platform.exit();
    System.exit(0);
  }

  /**
   * Xử lý luồng thêm sản phẩm lên Sàn đấu giá.
   *
   * @param event Sự kiện nhấn nút.
   */
  @FXML
  private void handleAddItem(ActionEvent event) {
    String name = txtName.getText().trim();
    String category = txtCategory.getText().trim();
    String priceText = txtPrice.getText().trim();

    if (name.isEmpty() || category.isEmpty() || priceText.isEmpty()) {
      showAlert(AlertType.WARNING, "Cảnh báo", "Vui lòng điền đầy đủ thông tin!");
      return;
    }

    try {
      double price = Double.parseDouble(priceText);

      // Tạo luồng mạng ngầm để không làm đơ giao diện
      new Thread(() -> {
        try {
          // Đóng gói gói tin JSON theo đúng chuẩn API thiết kế
          Message request = new Message();
          request.setAction("CREATE_AUCTION");
          request.setRole("SELLER");

          String randomId = "ITEM-" + (System.currentTimeMillis() % 10000);
          AuctionItem newItem = new AuctionItem(randomId, name, "Chưa có mô tả", category,
              "Mới", price);
          request.setItem(newItem);

          // Gửi yêu cầu qua Client và chờ Server trả lời
          Message response = AuctionClient.getInstance().sendRequest(request);

          Platform.runLater(() -> {
            if ("SUCCESS".equals(response.getStatus())) {
              showAlert(AlertType.INFORMATION, "Thành công", "Đã đưa sản phẩm lên sàn đấu giá!");
              txtName.clear();
              txtCategory.clear();
              txtPrice.clear();
            } else {
              showAlert(AlertType.ERROR, "Lỗi Server", "Thêm sản phẩm thất bại!");
            }
          });
        } catch (Exception e) {
          Platform.runLater(() -> {
            showAlert(AlertType.ERROR, "Lỗi mạng", "Mất kết nối: " + e.getMessage());
          });
        }
      }).start();

    } catch (NumberFormatException e) {
      showAlert(AlertType.ERROR, "Lỗi định dạng", "Giá khởi điểm bắt buộc phải là số!");
    }
  }

  /**
   * Xử lý đăng xuất về màn hình đăng nhập.
   *
   * @param event Sự kiện nhấn nút.
   */
  @FXML
  private void handleLogout(ActionEvent event) {
    try {
      Parent root = FXMLLoader.load(getClass().getResource("/com/auction/login.fxml"));
      Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
      stage.setScene(new Scene(root, 800, 600));
      stage.setTitle("Đăng nhập - Đấu giá trực tuyến");
      stage.centerOnScreen();
    } catch (IOException e) {
      showAlert(AlertType.ERROR, "Lỗi hệ thống", "Không thể tải màn hình đăng nhập!");
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