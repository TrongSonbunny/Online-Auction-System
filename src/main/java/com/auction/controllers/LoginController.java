package com.auction.controllers;

import java.io.IOException;
import java.util.HashMap;
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

  // Biến lưu tọa độ khi kéo cửa sổ đã được đổi tên thành offsetX và offsetY
  private double offsetX = 0.0;
  private double offsetY = 0.0;

  // Giả lập cơ sở dữ liệu người dùng tại local
  private static final Map<String, String> USER_DATABASE = new HashMap<>();

  static {
    USER_DATABASE.put("seller", "123456");
    USER_DATABASE.put("bidder", "123456");
  }

  /**
   * Phương thức chạy ngay khi FXML được load.
   * Dùng để thiết lập tính năng kéo thả cho thanh tiêu đề.
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

  /**
   * Thu nhỏ ứng dụng xuống taskbar.
   *
   * @param event Sự kiện click.
   */
  @FXML
  private void handleMinimize(ActionEvent event) {
    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    stage.setIconified(true);
  }

  /**
   * Phóng to hoặc thu nhỏ cửa sổ ứng dụng.
   *
   * @param event Sự kiện click.
   */
  @FXML
  private void handleMaximize(ActionEvent event) {
    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    stage.setMaximized(!stage.isMaximized());
  }

  /**
   * Tắt hoàn toàn ứng dụng.
   */
  @FXML
  private void handleClose() {
    Platform.exit();
    System.exit(0);
  }

  /**
   * Hiển thị giao diện form đăng nhập.
   */
  @FXML
  private void showLoginView() {
    loginForm.setVisible(true);
    registerForm.setVisible(false);
    btnToggleLogin.getStyleClass().add("btn-toggle-active");
    btnToggleRegister.getStyleClass().remove("btn-toggle-active");
    lblError.setText("");
  }

  /**
   * Hiển thị giao diện form đăng ký.
   */
  @FXML
  private void showRegisterView() {
    loginForm.setVisible(false);
    registerForm.setVisible(true);
    btnToggleRegister.getStyleClass().add("btn-toggle-active");
    btnToggleLogin.getStyleClass().remove("btn-toggle-active");
    lblError.setText("");
  }

  /**
   * Xử lý luồng đăng nhập của người dùng.
   */
  @FXML
  private void handleLogin() {
    String user = txtUsername.getText();
    String pass = txtPassword.getText();

    if (USER_DATABASE.containsKey(user) && USER_DATABASE.get(user).equals(pass)) {
      headerSection.setVisible(false);
      loginForm.setVisible(false);
      registerForm.setVisible(false);
      roleSelection.setVisible(true);
      lblError.setText("");
    } else {
      lblError.setStyle("-fx-text-fill: #ff4c4c;");
      lblError.setText("Sai tài khoản hoặc mật khẩu!");
    }
  }

  /**
   * Xử lý luồng đăng ký tài khoản mới.
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

    if (USER_DATABASE.containsKey(user)) {
      lblError.setStyle("-fx-text-fill: #ff4c4c;");
      lblError.setText("Tài khoản đã tồn tại!");
      return;
    }

    USER_DATABASE.put(user, pass);
    lblError.setStyle("-fx-text-fill: #27ae60;");
    lblError.setText("Đăng ký thành công! Hãy đăng nhập.");

    txtRegUser.clear();
    txtRegPass.clear();
    txtRegConfirm.clear();
    showLoginView();
  }

  /**
   * Xử lý sự kiện khi người dùng click chọn vai trò Bidder.
   *
   * @param event Sự kiện MouseEvent do JavaFX kích hoạt.
   */
  @FXML
  private void selectBidderRole(MouseEvent event) {
    try {
      switchScene(event, "/com/auction/primary.fxml", "Sàn đấu giá - Bidder");
    } catch (IOException e) {
      lblError.setStyle("-fx-text-fill: #ff4c4c;");
      lblError.setText("Lỗi tải giao diện Bidder!");
    }
  }

  /**
   * Xử lý sự kiện khi người dùng click chọn vai trò Seller.
   *
   * @param event Sự kiện MouseEvent do JavaFX kích hoạt.
   */
  @FXML
  private void selectSellerRole(MouseEvent event) {
    try {
      switchScene(event, "/com/auction/seller.fxml", "Quản lý sản phẩm - Seller");
    } catch (IOException e) {
      lblError.setStyle("-fx-text-fill: #ff4c4c;");
      lblError.setText("Lỗi tải giao diện Seller!");
    }
  }

  /**
   * Chuyển đổi Scene nhưng vẫn giữ nguyên kích thước của Stage hiện tại.
   *
   * @param event    Sự kiện MouseEvent.
   * @param fxmlPath Đường dẫn tới file FXML.
   * @param title    Tiêu đề mới cho cửa sổ.
   * @throws IOException Nếu không đọc được file FXML.
   */
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