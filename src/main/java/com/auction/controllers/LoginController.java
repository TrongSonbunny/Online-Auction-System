package com.auction.controllers;

import com.auction.App;
import com.auction.models.user.UserRole;
import com.auction.network.ActionType;
import com.auction.network.ClientMessage;
import com.auction.network.NetworkClient;
import com.auction.network.ServerMessage;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Điều khiển màn hình đăng nhập. Tích hợp Máy trạng thái (State Machine)
 * để xử lý linh hoạt trường hợp Backend không trả về Role sau khi Login.
 */
public class LoginController implements Initializable, NetworkClient.MessageListener {

  private static final Logger logger = Logger.getLogger(LoginController.class.getName());

  @FXML
  private VBox authContainer;
  @FXML
  private VBox roleSelection;
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
  @FXML
  private Label lblRoleTitle;
  @FXML
  private HBox titleBar;

  private AnimationTimer meshGradientTimer;
  private double gradientOffset = 0.0;

  private UserRole pendingRole;
  private boolean isRegisteringMode = false;

  @Override
  public void initialize(URL url, ResourceBundle rb) {
    NetworkClient.getInstance().addListener(this);
    setupUndecoratedWindowHandle(rb);

    Platform.runLater(() -> {
      Node root = titleBar.getScene().getRoot();
      if (root != null) {
        startMeshGradientAnimation(root);
      }
    });

    showLoginView();
  }

  private void startMeshGradientAnimation(Node targetNode) {
    meshGradientTimer = new AnimationTimer() {
      @Override
      public void handle(long now) {
        gradientOffset += 0.0005;
        targetNode.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, #0a0a0a, "
                + "rgba(26, 21, 5, " + (Math.sin(gradientOffset) * 0.1 + 0.9) + "), "
                + "rgba(5, 5, 5, " + (Math.cos(gradientOffset) * 0.1 + 0.9) + "));");
      }
    };
    meshGradientTimer.start();
  }

  @FXML
  private void handleLogin() {
    this.pendingRole = null;
    this.isRegisteringMode = false;

    String email = txtEmail.getText();
    String password = txtPassword.getText();

    if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
      showError("Vui lòng nhập đầy đủ email và mật khẩu.");
      triggerShakeAnimation(lblError);
      return;
    }

    ClientMessage loginRequest = ClientMessage.builder()
        .action(ActionType.LOGIN)
        .email(email)
        .password(password)
        .build();

    NetworkClient.getInstance().sendMessage(loginRequest);
  }

  @FXML
  private void handleRegisterStep1() {
    if (txtRegName.getText().isEmpty()
        || txtRegEmail.getText().isEmpty()
        || txtRegPassword.getText().isEmpty()) {
      showError("Vui lòng điền đầy đủ thông tin đăng ký.");
      triggerShakeAnimation(lblError);
      return;
    }

    this.isRegisteringMode = true;
    if (lblRoleTitle != null) {
      lblRoleTitle.setText("BƯỚC CUỐI: BẠN MUỐN ĐĂNG KÝ LÀ AI?");
    }
    showRoleSelection();
  }

  @FXML
  private void handleRoleBidder(MouseEvent event) {
    if (isRegisteringMode) {
      this.pendingRole = UserRole.BIDDER;
      sendRegisterRequest(UserRole.BIDDER);
    } else {
      navigateToMain("primary");
    }
  }

  @FXML
  private void handleRoleSeller(MouseEvent event) {
    if (isRegisteringMode) {
      this.pendingRole = UserRole.SELLER;
      sendRegisterRequest(UserRole.SELLER);
    } else {
      navigateToMain("seller");
    }
  }

  private void sendRegisterRequest(UserRole role) {
    ClientMessage registerRequest = ClientMessage.builder()
        .action(ActionType.REGISTER)
        .name(txtRegName.getText())
        .email(txtRegEmail.getText())
        .password(txtRegPassword.getText())
        .role(role)
        .build();

    NetworkClient.getInstance().sendMessage(registerRequest);
  }

  @Override
  public void onMessageReceived(ServerMessage message) {
    boolean isLogin = ActionType.LOGIN.name().equals(message.getAction());
    boolean isRegister = ActionType.REGISTER.name().equals(message.getAction());
    boolean isError = "EXECUTION_ERROR".equals(message.getAction());

    if (isLogin || isRegister || isError) {
      Platform.runLater(() -> {
        if (ServerMessage.STATUS_SUCCESS.equals(message.getStatus())) {
          handleSuccessResponse(isLogin, isRegister);
        } else {
          handleErrorResponse(message.getMessage());
        }
      });
    }
  }

  private void handleSuccessResponse(boolean isLogin, boolean isRegister) {
    if (isLogin) {
      App.loggedInEmail = txtEmail.getText();
      App.loggedInPassword = txtPassword.getText();

      if (this.pendingRole != null) {
        if (this.pendingRole == UserRole.SELLER) {
          navigateToMain("seller");
        } else {
          navigateToMain("primary");
        }
        this.pendingRole = null;
      } else {
        if (lblRoleTitle != null) {
          lblRoleTitle.setText("XÁC NHẬN CHUYÊN TRANG TRUY CẬP");
        }
        showRoleSelection();
      }

    } else if (isRegister) {
      ClientMessage autoLoginReq = ClientMessage.builder()
          .action(ActionType.LOGIN)
          .email(txtRegEmail.getText())
          .password(txtRegPassword.getText())
          .build();

      txtEmail.setText(txtRegEmail.getText());
      txtPassword.setText(txtRegPassword.getText());
      NetworkClient.getInstance().sendMessage(autoLoginReq);
    }
  }

  private void handleErrorResponse(String errorMsg) {
    if (errorMsg == null || errorMsg.isBlank()) {
      errorMsg = "Xác thực thất bại. Vui lòng kiểm tra lại!";
    }
    showError("LỖI: " + errorMsg);
    triggerShakeAnimation(lblError);
    logoutAndReturn();
  }

  private void triggerShakeAnimation(Node node) {
    TranslateTransition shake = new TranslateTransition(Duration.millis(100), node);
    shake.setFromX(0);
    shake.setToX(5);
    shake.setCycleCount(4);
    shake.setAutoReverse(true);
    shake.play();
  }

  @FXML
  private void showLoginView() {
    loginForm.setVisible(true);
    loginForm.setManaged(true);
    registerForm.setVisible(false);
    registerForm.setManaged(false);
    lblError.setText("");
  }

  @FXML
  private void showRegisterView() {
    loginForm.setVisible(false);
    loginForm.setManaged(false);
    registerForm.setVisible(true);
    registerForm.setManaged(true);
    lblError.setText("");
  }

  private void showRoleSelection() {
    if (authContainer != null && roleSelection != null) {
      authContainer.setVisible(false);
      roleSelection.setVisible(true);
      roleSelection.setOpacity(0);
      FadeTransition ft = new FadeTransition(Duration.millis(500), roleSelection);
      ft.setFromValue(0);
      ft.setToValue(1);
      ft.play();
    }
  }

  @FXML
  private void logoutAndReturn() {
    if (authContainer != null && roleSelection != null) {
      roleSelection.setVisible(false);
      authContainer.setVisible(true);
      lblError.setText("");
    }
  }

  private void navigateToMain(String fxmlTarget) {
    try {
      if (meshGradientTimer != null) {
        meshGradientTimer.stop();
      }
      App.setRoot(fxmlTarget);
    } catch (IOException e) {
      showError("Lỗi hệ thống: Không thể chuyển giao diện.");
    }
  }

  private void showError(String error) {
    lblError.setText(error);
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
    if (meshGradientTimer != null) {
      meshGradientTimer.stop();
    }
    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    stage.close();
  }

  private double xOffset = 0;
  private double yOffset = 0;

  private void setupUndecoratedWindowHandle(ResourceBundle rb) {
    if (titleBar != null) {
      titleBar.setOnMousePressed(event -> {
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
      });
      titleBar.setOnMouseDragged(event -> {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setX(event.getScreenX() - xOffset);
        stage.setY(event.getScreenY() - yOffset);
      });
    }
  }
}