package com.auction.controllers;

import com.auction.App;
import com.auction.network.ActionType;
import com.auction.network.ClientMessage;
import com.auction.network.NetworkClient;
import com.auction.network.ServerMessage;
import java.io.IOException;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Controller xử lý màn hình quản lý dành cho Người bán (Seller).
 * Tuân thủ nghiêm ngặt các trường validate từ CreateAuctionCommand.
 */
public class SellerController implements NetworkClient.MessageListener {

  @FXML
  private TextField txtName;

  @FXML
  private TextField txtCategory;

  @FXML
  private TextField txtStartingPrice;

  @FXML
  private Label lblStatus;

  /**
   * Khởi tạo giao diện và đăng ký lắng nghe sự kiện mạng.
   */
  @FXML
  public void initialize() {
    NetworkClient.getInstance().addListener(this);
  }

  /**
   * Gửi yêu cầu khởi tạo một phiên đấu giá mới.
   */
  @FXML
  private void handleCreateAuction() {
    try {
      double startingPrice = Double.parseDouble(txtStartingPrice.getText());

      ClientMessage createRequest = ClientMessage.builder()
          .action(ActionType.CREATE_AUCTION)
          .userId("CURRENT_SELLER") // Backend sẽ cần ID user thực tế
          .itemName(txtName.getText())
          .itemCategory(txtCategory.getText())
          .startingPrice(startingPrice)
          .itemDescription("Mô tả mặc định từ hệ thống")
          .itemCondition("Mới")
          .estimatedPrice(startingPrice * 1.5)
          .durationSeconds(86400) // Đấu giá diễn ra trong 24 giờ
          .build();

      NetworkClient.getInstance().sendMessage(createRequest);
      setStatus("Đang gửi yêu cầu tạo đấu giá...", false);

    } catch (NumberFormatException e) {
      setStatus("Lỗi: Giá khởi điểm phải là một số hợp lệ.", true);
    }
  }

  /**
   * Xử lý kết quả trả về từ máy chủ sau khi yêu cầu tạo sản phẩm.
   *
   * @param response gói dữ liệu phản hồi từ máy chủ
   */
  @Override
  public void onMessageReceived(ServerMessage response) {
    if (ActionType.CREATE_AUCTION.name().equals(response.getAction())) {
      Platform.runLater(() -> {
        if (ServerMessage.STATUS_SUCCESS.equals(response.getStatus())) {
          setStatus("Tạo phiên đấu giá thành công!", false);
          clearInputs();
        } else {
          setStatus("Lỗi: " + response.getMessage(), true);
        }
      });
    }
  }

  /**
   * Cập nhật trạng thái hiển thị trên giao diện theo màu sắc.
   *
   * @param msg     nội dung thông báo
   * @param isError đổi màu chữ đỏ nếu là thông báo lỗi
   */
  private void setStatus(String msg, boolean isError) {
    if (lblStatus != null) {
      lblStatus.setStyle(isError ? "-fx-text-fill: #ff4c4c;" : "-fx-text-fill: #28a745;");
      lblStatus.setText(msg);
    }
  }

  /**
   * Xóa nội dung của các ô nhập liệu.
   */
  private void clearInputs() {
    if (txtName != null) {
      txtName.clear();
    }
    if (txtCategory != null) {
      txtCategory.clear();
    }
    if (txtStartingPrice != null) {
      txtStartingPrice.clear();
    }
  }

  /**
   * Xử lý đăng xuất và chuyển về màn hình đăng nhập.
   *
   * @param event sự kiện click chuột
   */
  @FXML
  private void handleLogout(ActionEvent event) {
    try {
      App.setRoot("login");
    } catch (IOException e) {
      setStatus("Không thể thoát lúc này.", true);
    }
  }
}