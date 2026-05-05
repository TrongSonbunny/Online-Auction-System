package com.auction;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller xử lý các sự kiện giao diện cho màn hình đăng nhập.
 */
public class LoginController {

  @FXML
  private TextField txtUsername;

  @FXML
  private PasswordField txtPassword;

  @FXML
  private Label lblError;

  /**
   * Xử lý sự kiện khi người dùng nhấn nút đăng nhập.
   * Xác thực thông tin tài khoản và chuyển hướng nếu thành công.
   *
   * @throws IOException Nếu có lỗi trong quá trình tải giao diện mới
   */
  @FXML
  private void handleLogin() throws IOException {
    String username = txtUsername.getText();
    String password = txtPassword.getText();

    // Sử dụng hằng số chuỗi gọi phương thức equals để tránh NullPointerException
    if ("admin".equals(username) && "123456".equals(password)) {
      System.out.println("Đăng nhập thành công!");
      App.setRoot("primary");
    } else {
      lblError.setText("Sai tài khoản hoặc mật khẩu!");
    }
  }
}