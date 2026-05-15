package com.auction.controllers;

import com.auction.App;
import com.auction.models.user.UserRole;
import com.auction.network.ActionType;
import com.auction.network.ClientMessage;
import com.auction.network.NetworkClient;
import com.auction.network.ServerMessage;
import java.io.IOException;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Controller xử lý logic cho màn hình đăng nhập, đăng ký và chọn vai trò.
 * Đảm bảo giao tiếp với server qua NetworkClient.
 */
public class LoginController implements NetworkClient.MessageListener {

  @FXML
  private VBox loginForm;

  @FXML
  private VBox registerForm;

  @FXML
  private TextField txtEmail;

  @FXML
  private PasswordField txtPassword;

  @FXML
  private TextField txtRegName;

  @FXML
  private TextField txtRegEmail;

  @FXML
  private PasswordField txtRegPassword;

  @FXML
  private Label lblError;

  /**
   * Khởi tạo giao diện, mặc định hiển thị form đăng nhập và lắng nghe dữ liệu
   * mạng.
   */
  @FXML
  public void initialize() {
    NetworkClient.getInstance().addListener(this);
    if (loginForm != null && registerForm != null) {
      showLoginView(null);
    }
  }

  /**
   * Xử lý gửi yêu cầu đăng nhập.
   */
  @FXML
  private void handleLogin() {
    if (txtEmail == null || txtPassword == null) {
      return;
    }

    String email = txtEmail.getText();
    String password = txtPassword.getText();

    if (email.isEmpty() || password.isEmpty()) {
      showError("Vui lòng nhập đầy đủ email và mật khẩu.");
      return;
    }

    ClientMessage loginRequest = ClientMessage.builder()
        .action(ActionType.LOGIN)
        .email(email)
        .password(password)
        .build();

    NetworkClient.getInstance().sendMessage(loginRequest);
  }

  /**
   * Xử lý gửi yêu cầu đăng ký tài khoản mới.
   */
  @FXML
  private void handleRegister() {
    if (txtRegName == null || txtRegEmail == null || txtRegPassword == null) {
      return;
    }

    String name = txtRegName.getText();
    String email = txtRegEmail.getText();
    String password = txtRegPassword.getText();

    if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
      showError("Vui lòng điền đầy đủ thông tin đăng ký.");
      return;
    }

    ClientMessage registerRequest = ClientMessage.builder()
        .action(ActionType.REGISTER)
        .name(name)
        .email(email)
        .password(password)
        .role(UserRole.BIDDER) // Mặc định role là BIDDER
        .build();

    NetworkClient.getInstance().sendMessage(registerRequest);
  }

  /**
   * Phân phối và xử lý kết quả phản hồi từ máy chủ.
   *
   * @param message dữ liệu trả về từ máy chủ
   */
  @Override
  public void onMessageReceived(ServerMessage message) {
    if (ActionType.LOGIN.name().equals(message.getAction())
        || ActionType.REGISTER.name().equals(message.getAction())) {
      Platform.runLater(() -> {
        if (ServerMessage.STATUS_SUCCESS.equals(message.getStatus())) {
          navigateToMain(message.getRole());
        } else {
          showError(message.getMessage());
        }
      });
    }
  }

  /**
   * Chuyển hướng người dùng sang màn hình tương ứng với chức năng.
   *
   * @param role vai trò của người dùng (SELLER hoặc BIDDER)
   */
  private void navigateToMain(UserRole role) {
    try {
      if (role == UserRole.SELLER) {
        App.setRoot("seller");
      } else {
        App.setRoot("primary");
      }
    } catch (IOException e) {
      showError("Lỗi hệ thống: Không thể chuyển giao diện.");
    }
  }

  /**
   * Hiển thị thông báo lỗi lên giao diện.
   *
   * @param msg nội dung lỗi
   */
  private void showError(String msg) {
    if (lblError != null) {
      lblError.setStyle("-fx-text-fill: #ff4c4c;");
      lblError.setText(msg);
    }
  }

  // --- CÁC HÀM ĐIỀU KHIỂN CHUYỂN ĐỔI FORM VÀ CỬA SỔ TỪ FXML ---

  @FXML
  private void showRegisterView(ActionEvent event) {
    if (loginForm != null && registerForm != null) {
      loginForm.setVisible(false);
      loginForm.setManaged(false);
      registerForm.setVisible(true);
      registerForm.setManaged(true);
      lblError.setText("");
    }
  }

  @FXML
  private void showLoginView(ActionEvent event) {
    if (loginForm != null && registerForm != null) {
      registerForm.setVisible(false);
      registerForm.setManaged(false);
      loginForm.setVisible(true);
      loginForm.setManaged(true);
      lblError.setText("");
    }
  }

  @FXML
  private void selectBidderRole(MouseEvent event) throws IOException {
    App.setRoot("primary");
  }

  @FXML
  private void selectSellerRole(MouseEvent event) throws IOException {
    App.setRoot("seller");
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
  private void handleClose(ActionEvent event) {
    Platform.exit();
    System.exit(0);
  }
}