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
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * Điều khiển màn hình hiển thị và tạo phiên đấu giá dành cho người bán
 * (Seller).
 */
public class SellerController implements Initializable, NetworkClient.MessageListener {

  @FXML
  private TextField txtName;
  @FXML
  private TextField txtCategory;
  @FXML
  private TextField txtStartingPrice;
  @FXML
  private Label lblStatus;
  @FXML
  private HBox titleBar;

  private AnimationTimer meshGradientTimer;
  private double gradientOffset = 0.0;

  private double offsetX = 0;
  private double offsetY = 0;

  @Override
  public void initialize(URL url, ResourceBundle rb) {
    NetworkClient.getInstance().addListener(this);
    setupUndecoratedWindowHandle();

    Platform.runLater(() -> {
      // ĐÃ FIX: Lớp giáp bảo vệ kiểm tra Null trước khi chọc vào Scene
      if (titleBar != null && titleBar.getScene() != null) {
        Node root = titleBar.getScene().getRoot();
        if (root != null) {
          startMeshGradientAnimation(root);
        }
      }
    });
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
  private void handleCreateAuction() {
    if (txtName.getText().isEmpty()
        || txtCategory.getText().isEmpty()
        || txtStartingPrice.getText().isEmpty()) {
      setStatus("Vui lòng điền đầy đủ thông tin vật phẩm!", true);
      return;
    }

    try {
      double startingPrice = Double.parseDouble(txtStartingPrice.getText());

      ClientMessage createRequest = ClientMessage.builder()
          .action(ActionType.CREATE_AUCTION)
          .userId(App.loggedInUserId)
          .email(App.loggedInEmail)
          .password(App.loggedInPassword)
          .role(UserRole.SELLER)
          .itemName(txtName.getText())
          .itemCategory(txtCategory.getText().toUpperCase())
          .itemDescription("Vật phẩm đấu giá độc bản")
          .itemCondition("NEW")
          .estimatedPrice(startingPrice * 1.5)
          .startingPrice(startingPrice)
          .durationSeconds(3600)
          .build();

      NetworkClient.getInstance().sendMessage(createRequest);
    } catch (NumberFormatException e) {
      setStatus("CẢNH BÁO: Định dạng giá tiền không hợp lệ!", true);
    }
  }

  @Override
  public void onMessageReceived(ServerMessage response) {
    if (ActionType.CREATE_AUCTION.name().equals(response.getAction())
        || "EXECUTION_ERROR".equals(response.getAction())) {

      Platform.runLater(() -> {
        if (ServerMessage.STATUS_SUCCESS.equals(response.getStatus())) {
          setStatus("THÀNH CÔNG! Phiên đấu giá đã lên sóng radar.", false);
          clearInputs();
        } else {
          setStatus("TỪ CHỐI: " + response.getMessage(), true);
        }
      });
    }
  }

  private void setStatus(String msg, boolean isError) {
    if (lblStatus != null) {
      lblStatus.setStyle(isError ? "-fx-text-fill: #ff4444;" : "-fx-text-fill: #d4af37;");
      lblStatus.setText(msg);
    }
  }

  private void clearInputs() {
    txtName.clear();
    txtCategory.clear();
    txtStartingPrice.clear();
  }

  @FXML
  private void handleLogout(ActionEvent event) {
    try {
      App.loggedInEmail = null;
      App.loggedInPassword = null;
      App.loggedInUserId = null;
      if (meshGradientTimer != null) {
        meshGradientTimer.stop();
      }
      App.setRoot("login");
    } catch (IOException e) {
      setStatus("Lỗi hệ thống khi đăng xuất.", true);
    }
  }

  @FXML
  private void handleMinimize(ActionEvent event) {
    ((Stage) ((Node) event.getSource()).getScene().getWindow()).setIconified(true);
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
    ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
  }

  private void setupUndecoratedWindowHandle() {
    if (titleBar != null) {
      titleBar.setOnMousePressed(event -> {
        offsetX = event.getSceneX();
        offsetY = event.getSceneY();
      });
      titleBar.setOnMouseDragged(event -> {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setX(event.getScreenX() - offsetX);
        stage.setY(event.getScreenY() - offsetY);
      });
    }
  }
}