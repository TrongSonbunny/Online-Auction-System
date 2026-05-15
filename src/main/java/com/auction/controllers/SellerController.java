package com.auction.controllers;

import com.auction.network.ActionType;
import com.auction.network.ClientMessage;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller xử lý màn hình dành cho Người bán (Seller).
 * Cho phép nhập và gửi lệnh CREATE_AUCTION qua Socket.
 */
public class SellerController {

  @FXML
  private TextField txtName;
  @FXML
  private TextField txtCategory;
  @FXML
  private TextField txtStartingPrice;
  @FXML
  private Label lblStatus;

  private Socket socket;
  private ObjectOutputStream out;

  /**
   * Khởi tạo kết nối mạng cho Seller.
   */
  @FXML
  public void initialize() {
    try {
      socket = new Socket("127.0.0.1", 8080);
      out = new ObjectOutputStream(socket.getOutputStream());
      System.out.println("Seller đã kết nối tới Server thành công.");
    } catch (IOException e) {
      lblStatus.setStyle("-fx-text-fill: red;");
      lblStatus.setText("Không thể kết nối tới Server!");
    }
  }

  /**
   * Xử lý sự kiện khi Seller nhấn nút "Thêm Sản Phẩm".
   */
  @FXML
  private void handleAddItem() {
    String name = txtName.getText();
    String category = txtCategory.getText();
    String priceText = txtStartingPrice.getText();

    if (name.trim().isEmpty() || category.trim().isEmpty() || priceText.trim().isEmpty()) {
      lblStatus.setStyle("-fx-text-fill: red;");
      lblStatus.setText("Vui lòng điền đầy đủ thông tin!");
      return;
    }

    if (out == null) {
      lblStatus.setStyle("-fx-text-fill: red;");
      lblStatus.setText("Chưa kết nối Server. Không thể gửi!");
      return;
    }

    try {
      double price = Double.parseDouble(priceText);

      // Tạo ClientMessage đầy đủ dữ liệu (Gán cứng các trường UI chưa hỗ trợ)
      ClientMessage request = ClientMessage.builder()
          .action(ActionType.CREATE_AUCTION)
          .userId("USER_ID_TU_SESSION") // Thay bằng ID thực tế lấy từ Session
          .itemName(name)
          .itemCategory(category.toUpperCase())
          .itemDescription("Sản phẩm được đăng bán bởi Seller")
          .itemCondition("NEW")
          .estimatedPrice(price * 1.5) // Giá ước tính
          .startingPrice(price)
          .durationSeconds(86400) // Đấu giá trong 24 tiếng
          .build();

      out.reset();
      out.writeObject(request);
      out.flush();

      lblStatus.setStyle("-fx-text-fill: green;");
      lblStatus.setText("Lệnh tạo đấu giá đã được gửi: " + name);

      txtName.clear();
      txtCategory.clear();
      txtStartingPrice.clear();

    } catch (NumberFormatException e) {
      lblStatus.setStyle("-fx-text-fill: red;");
      lblStatus.setText("Giá khởi điểm phải là một số hợp lệ!");
    } catch (Exception e) {
      e.printStackTrace();
      lblStatus.setStyle("-fx-text-fill: red;");
      lblStatus.setText("Lỗi mạng: " + e.getMessage());
    }
  }

  @FXML
  private void handleLogout(ActionEvent event) {
    try {
      // 1. Đóng kết nối Server trước khi thoát
      if (socket != null && !socket.isClosed()) {
        socket.close();
      }

      // 2. Tải giao diện Login (Đã thêm /views/)
      Parent loginRoot = FXMLLoader.load(
          getClass().getResource("/com/auction/views/login.fxml"));

      // 3. Lấy Stage hiện tại thông qua ActionEvent
      Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

      // 4. Bắt lại kích thước TOÀN BỘ CỬA SỔ
      double currentWidth = stage.getWidth();
      double currentHeight = stage.getHeight();

      // 5. Cài đặt Scene mới
      stage.setScene(new Scene(loginRoot));

      // 6. Ép lại kích thước cũ trực tiếp cho Stage
      stage.setWidth(currentWidth);
      stage.setHeight(currentHeight);
      stage.setTitle("Đăng nhập - Đấu giá trực tuyến");

    } catch (IOException e) {
      lblStatus.setStyle("-fx-text-fill: red;");
      lblStatus.setText("Không thể đăng xuất!");
      e.printStackTrace();
    }
  }
}