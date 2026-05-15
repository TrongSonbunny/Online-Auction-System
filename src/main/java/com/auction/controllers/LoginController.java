package com.auction.controllers;

import com.auction.models.Message;
import com.auction.network.AuctionClient;
import java.io.IOException;
import java.util.Map;
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

  @FXML
  private HBox titleBar;

  @FXML
  private VBox mainCard;

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
   * Phương thức chạy ngay khi FXML được load.
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
   * Xử lý luồng đăng nhập bằng Đa luồng (Background Thread) để không đơ UI.
   */
  @FXML
  private void handleLogin() {
    String user = txtUsername.getText();
    String pass = txtPassword.getText();

    if (user.isEmpty() || pass.isEmpty()) {
      lblError.setStyle("-fx-text-fill: #ff4c4c;");
      lblError.setText("Không được để trống!");
      return;
    }

    lblError.setStyle("-fx-text-fill: #00c6ff;");
    lblError.setText("Đang kết nối Server...");

    // Tạo luồng mạng chạy ngầm
    new Thread(() -> {
      try {
        Message request = new Message();
        request.setAction("LOGIN_REQUEST");
        request.setRole("UNKNOWN");
        request.setUsername(user);
        request.setPassword(pass);

        Message response = AuctionClient.getInstance().sendRequest(request);

        // Đẩy kết quả ngược lại luồng UI (FX Thread)
        Platform.runLater(() -> {
          if ("SUCCESS".equals(response.getStatus())) {
            headerSection.setVisible(false);
            loginForm.setVisible(false);
            registerForm.setVisible(false);
            roleSelection.setVisible(true);
            lblError.setText("");
          } else {
            lblError.setStyle("-fx-text-fill: #ff4c4c;");
            lblError.setText("Đăng nhập thất bại (Tài khoản/MK sai)");
          }
        });
      } catch (Exception e) {
        Platform.runLater(() -> {
          lblError.setStyle("-fx-text-fill: #ff4c4c;");
          lblError.setText("Lỗi mạng: " + e.getMessage());
        });
      }
    }).start();
  }

  /**
   * Xử lý luồng đăng ký bằng Đa luồng (Background Thread) để không đơ UI.
   */
  @FXML
  private void handleRegister() {
    String user = txtRegUser.getText().trim();
    String pass = txtRegPass.getText();
    String confirm = txtRegConfirm.getText();

    if (user.isEmpty() || pass.isEmpty()) {
      lblError.setStyle("-fx-text-fill: #ff4c4c;");
      lblError.setText("Không được để trống thông tin!");
      return;
    }

    if (!pass.equals(confirm)) {
      lblError.setStyle("-fx-text-fill: #ff4c4c;");
      lblError.setText("Mật khẩu xác nhận không khớp!");
      return;
    }

    lblError.setStyle("-fx-text-fill: #00c6ff;");
    lblError.setText("Đang đăng ký...");

    // Tạo luồng mạng chạy ngầm
    new Thread(() -> {
      try {
        Message request = new Message();
        request.setAction("REGISTER");
        request.setRole("BIDDER");
        request.setUsername(user);
        request.setPassword(pass);

        Message response = AuctionClient.getInstance().sendRequest(request);

        // Đẩy kết quả ngược lại luồng UI (FX Thread)
        Platform.runLater(() -> {
          if ("SUCCESS".equals(response.getStatus())) {
            lblError.setStyle("-fx-text-fill: #27ae60;");
            lblError.setText("Đăng ký thành công! Hãy đăng nhập.");
            txtRegUser.clear();
            txtRegPass.clear();
            txtRegConfirm.clear();
            showLoginView();
          } else {
            lblError.setStyle("-fx-text-fill: #ff4c4c;");
            lblError.setText("Tài khoản đã tồn tại!");
          }
        });
      } catch (Exception e) {
        Platform.runLater(() -> {
          lblError.setStyle("-fx-text-fill: #ff4c4c;");
          lblError.setText("Lỗi mạng: " + e.getMessage());
        });
      }
    }).start();
  }

  @FXML
  private void selectBidderRole(MouseEvent event) {
    try {
      switchScene(event, "/com/auction/primary.fxml", "Sàn đấu giá - Bidder");
    } catch (IOException e) {
      // THÊM DÒNG NÀY ĐỂ IN LỖI ĐỎ THỰC SỰ RA TERMINAL
      e.printStackTrace();

      lblError.setStyle("-fx-text-fill: #ff4c4c;");
      lblError.setText("Lỗi tải giao diện Bidder!");
    }
  }

  @FXML
  private void selectSellerRole(MouseEvent event) {
    try {
      switchScene(event, "/com/auction/seller.fxml", "Quản lý sản phẩm - Seller");
    } catch (IOException e) {
      // THÊM DÒNG NÀY ĐỂ IN LỖI ĐỎ THỰC SỰ RA TERMINAL
      e.printStackTrace();

      lblError.setStyle("-fx-text-fill: #ff4c4c;");
      lblError.setText("Lỗi tải giao diện Seller!");
    }
  }

  private void switchScene(MouseEvent event, String fxmlPath, String title) throws IOException {
    Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

    double currentWidth = stage.getWidth();
    double currentHeight = stage.getHeight();

    stage.setScene(new Scene(root));
    stage.setWidth(currentWidth);
    stage.setHeight(currentHeight);
    stage.setTitle(title);
  }
}