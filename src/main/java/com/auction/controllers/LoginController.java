package com.auction.controllers;

import com.auction.exceptions.AuthenticationException;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller xử lý logic cho màn hình đăng nhập.
 */
public class LoginController {

  @FXML
  private TextField txtUsername;

  @FXML
  private PasswordField txtPassword;

  @FXML
  private Label lblError;

  /**
   * Xử lý sự kiện khi người dùng nhấn nút "Đăng nhập".
   *
   * @param event Sự kiện từ hệ thống JavaFX.
   */
  @FXML
  private void handleLogin(ActionEvent event) {
    String username = txtUsername.getText();
    String password = txtPassword.getText();

    try {
      // 1. Xác thực tài khoản
      String role = authenticate(username, password);
      lblError.setText("");
      System.out.println("Đăng nhập thành công: " + username + " (Vai trò: " + role + ")");

      // 2. Xác định đường dẫn FXML và Tiêu đề
      String fxmlPath = "/com/auction/primary.fxml";
      String title = "Sàn đấu giá - Bidder";

      if ("SELLER".equals(role)) {
        fxmlPath = "/com/auction/seller.fxml";
        title = "Quản lý sản phẩm - Seller";
      }

      // 3. Chuyển màn hình và giữ nguyên kích thước cửa sổ hiện tại
      switchScene(event, fxmlPath, title);

    } catch (AuthenticationException e) {
      lblError.setText(e.getMessage());
      lblError.setStyle("-fx-text-fill: red;");
    } catch (IOException e) {
      System.err.println("Lỗi chuyển trang: " + e.getMessage());
      lblError.setText("Lỗi hệ thống: Không thể tải giao diện.");
    }
  }

  /**
   * Thực hiện chuyển đổi Scene trong khi bảo toàn kích thước Stage.
   *
   * @param event    Sự kiện kích hoạt.
   * @param fxmlPath Đường dẫn tới file FXML mới.
   * @param title    Tiêu đề mới của cửa sổ.
   * @throws IOException Nếu không tìm thấy file FXML.
   */
  private void switchScene(ActionEvent event, String fxmlPath, String title) throws IOException {
    Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));

    // Lấy Stage hiện tại
    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

    // 1. Lưu lại kích thước TOÀN BỘ CỬA SỔ
    double currentWidth = stage.getWidth();
    double currentHeight = stage.getHeight();

    // 2. Tạo Scene mới KHÔNG kèm kích thước để tránh lỗi cộng dồn viền
    stage.setScene(new Scene(root));

    // 3. Ép lại kích thước cũ trực tiếp cho Stage
    stage.setWidth(currentWidth);
    stage.setHeight(currentHeight);

    stage.setTitle(title);
  }

  /**
   * Kiểm tra thông tin đăng nhập và trả về vai trò của người dùng.
   *
   * @param username Tên đăng nhập.
   * @param password Mật khẩu.
   * @return Vai trò người dùng ("SELLER" hoặc "BIDDER").
   * @throws AuthenticationException Nếu thông tin sai hoặc để trống.
   */
  private String authenticate(String username, String password) throws AuthenticationException {
    if (username == null || username.trim().isEmpty()
        || password == null || password.trim().isEmpty()) {
      throw new AuthenticationException("Vui lòng nhập đầy đủ tài khoản và mật khẩu!");
    }

    if ("seller".equals(username) && "123456".equals(password)) {
      return "SELLER";
    } else if ("bidder".equals(username) && "123456".equals(password)) {
      return "BIDDER";
    }

    throw new AuthenticationException("Sai thông tin! Thử lại với seller/123456.");
  }
}