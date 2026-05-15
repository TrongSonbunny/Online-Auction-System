package com.auction.controllers;

import com.auction.models.user.User;
import com.auction.models.user.UserRole;
import com.auction.network.ActionType;
import com.auction.network.ClientMessage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Controller xử lý logic cho màn hình đăng nhập, đăng ký và chọn vai trò.
 */
public class LoginController {

  private static final String SERVER_IP = "127.0.0.1";
  private static final int SERVER_PORT = 8080;

  @FXML
  private HBox titleBar;
  @FXML
  private VBox headerSection;
  @FXML
  private VBox loginForm;
  @FXML
  private VBox registerForm;
  @FXML
  private VBox roleSelection;
  @FXML
  private Button btnToggleLogin;
  @FXML
  private Button btnToggleRegister;
  @FXML
  private TextField txtUsername;
  @FXML
  private PasswordField txtPassword;
  @FXML
  private TextField txtRegUser;
  @FXML
  private PasswordField txtRegPass;
  @FXML
  private PasswordField txtRegConfirm;
  @FXML
  private Label lblError;

  private double offsetX = 0.0;
  private double offsetY = 0.0;

  /**
   * Phương thức khởi tạo, gán sự kiện kéo thả cho thanh tiêu đề.
   */
  @FXML
  public void initialize() {
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

  @FXML
  private void showLoginView() {
    loginForm.setVisible(true);
    registerForm.setVisible(false);
    btnToggleLogin.getStyleClass().add("btn-toggle-active");
    btnToggleRegister.getStyleClass().remove("btn-toggle-active");
    lblError.setText("");
  }

  @FXML
  private void showRegisterView() {
    loginForm.setVisible(false);
    registerForm.setVisible(true);
    btnToggleRegister.getStyleClass().add("btn-toggle-active");
    btnToggleLogin.getStyleClass().remove("btn-toggle-active");
    lblError.setText("");
  }

  /**
   * Thực hiện đăng nhập bằng cách tự mở Socket gửi ClientMessage.
   */
  @FXML
  private void handleLogin() {
    String email = txtUsername.getText();
    String pass = txtPassword.getText();

    if (email.isEmpty() || pass.isEmpty()) {
      setError("Không được để trống!");
      return;
    }

    lblError.setStyle("-fx-text-fill: #00c6ff;");
    lblError.setText("Đang kết nối Server...");

    new Thread(() -> {
      try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
          ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
          ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

        ClientMessage request = ClientMessage.builder()
            .action(ActionType.LOGIN)
            .email(email)
            .password(pass)
            .build();

        out.writeObject(request);
        out.flush();

        Object response = in.readObject();

        Platform.runLater(() -> {
          if (response instanceof User) {
            headerSection.setVisible(false);
            loginForm.setVisible(false);
            registerForm.setVisible(false);
            roleSelection.setVisible(true);
            lblError.setText("");
          } else if (response instanceof Exception) {
            Exception e = (Exception) response; // Ép kiểu thủ công để lấy thông báo lỗi chính xác từ Backend
            // Hiển thị LỖI THỰC SỰ TỪ SERVER
            setError(e.getMessage());
          } else {
            setError("Tài khoản hoặc mật khẩu không chính xác!");
          }
        });
      } catch (Exception e) {
        Platform.runLater(() -> setError("Lỗi kết nối Server: " + e.getMessage()));
      }
    }).start();
  }

  /**
   * Thực hiện đăng ký bằng cách tự mở Socket gửi ClientMessage.
   */
  @FXML
  private void handleRegister() {
    String email = txtRegUser.getText().trim();
    String pass = txtRegPass.getText();
    String confirm = txtRegConfirm.getText();

    if (email.isEmpty() || pass.isEmpty()) {
      setError("Vui lòng điền đủ thông tin!");
      return;
    }

    if (!pass.equals(confirm)) {
      setError("Mật khẩu không khớp!");
      return;
    }

    lblError.setStyle("-fx-text-fill: #00c6ff;");
    lblError.setText("Đang đăng ký...");

    new Thread(() -> {
      try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
          ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
          ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

        ClientMessage request = ClientMessage.builder()
            .action(ActionType.REGISTER)
            .role(UserRole.BIDDER)
            .name("Người Dùng Mới")
            .email(email)
            .password(pass)
            .build();

        out.writeObject(request);
        out.flush();

        Object response = in.readObject();

        Platform.runLater(() -> {
          if (response instanceof User) {
            lblError.setStyle("-fx-text-fill: #27ae60;");
            lblError.setText("Đăng ký thành công! Hãy đăng nhập.");
            txtRegUser.clear();
            txtRegPass.clear();
            txtRegConfirm.clear();
            showLoginView();
          } else if (response instanceof Exception) {
            // FIX LỖI: Trích xuất thông báo lỗi chính xác từ Backend
            Exception e = (Exception) response; // Ép kiểu thủ công để lấy thông báo lỗi chính xác từ Backend
            setError(e.getMessage());
          } else {
            setError("Đăng ký thất bại: Phản hồi không hợp lệ.");
          }
        });
      } catch (Exception e) {
        Platform.runLater(() -> setError("Lỗi kết nối Server: " + e.getMessage()));
      }
    }).start();
  }

  private void setError(String message) {
    lblError.setStyle("-fx-text-fill: #ff4c4c;");
    lblError.setText(message);
  }

  @FXML
  private void selectBidderRole(MouseEvent event) {
    try {
      switchScene(event, "/com/auction/views/primary.fxml", "Sàn đấu giá - Bidder");
    } catch (IOException e) {
      setError("Không thể tải giao diện Bidder!");
    }
  }

  @FXML
  private void selectSellerRole(MouseEvent event) {
    try {
      switchScene(event, "/com/auction/views/seller.fxml", "Quản lý sản phẩm - Seller");
    } catch (IOException e) {
      setError("Không thể tải giao diện Seller!");
    }
  }

  private void switchScene(MouseEvent event, String fxmlPath, String title) throws IOException {
    Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    stage.setScene(new Scene(root));
    stage.setTitle(title);
    stage.centerOnScreen();
  }
}