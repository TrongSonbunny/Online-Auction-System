package com.auction.controllers;

import com.auction.models.Message;
import com.auction.models.item.AuctionItem;
import com.auction.network.AuctionClient;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * Controller xử lý giao diện Sàn đấu giá dành cho Bidder.
 * Tích hợp lấy dữ liệu JSON qua lớp trung gian AuctionClient.
 */
public class PrimaryController implements Initializable {

  @FXML
  private HBox titleBar;

  @FXML
  private TableView<AuctionItem> productTable;

  @FXML
  private TableColumn<AuctionItem, String> colId;

  @FXML
  private TableColumn<AuctionItem, String> colName;

  @FXML
  private TableColumn<AuctionItem, Double> colPrice;

  @FXML
  private TextField txtBidAmount;

  @FXML
  private Label lblMessage;

  private double offsetX = 0.0;
  private double offsetY = 0.0;
  private ObservableList<AuctionItem> tableData;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    setupWindowDragging();

    // Map các cột với các biến trong class AuctionItem
    colId.setCellValueFactory(new PropertyValueFactory<>("itemId"));
    colName.setCellValueFactory(new PropertyValueFactory<>("name"));
    colPrice.setCellValueFactory(new PropertyValueFactory<>("estimatedPrice"));

    tableData = FXCollections.observableArrayList();
    productTable.setItems(tableData);

    // Tự động kéo dữ liệu từ Server khi vừa mở màn hình
    loadData();
  }

  /**
   * Thiết lập tính năng kéo thả cửa sổ bằng thanh tiêu đề.
   */
  private void setupWindowDragging() {
    if (titleBar != null) {
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

  /**
   * Chạy luồng ngầm lấy toàn bộ phiên đấu giá bằng gói tin JSON.
   */
  @FXML
  private void loadData() {
    lblMessage.setStyle("-fx-text-fill: #00c6ff;");
    lblMessage.setText("Đang tải dữ liệu từ Server...");

    new Thread(() -> {
      try {
        Message request = new Message();
        request.setAction("GET_ALL_AUCTIONS_REQUEST"); // Hành động gửi lên

        Message response = AuctionClient.getInstance().sendRequest(request);

        Platform.runLater(() -> {
          if ("SUCCESS".equals(response.getStatus()) && response.getData() != null) {
            tableData.clear();
            List<?> list = (List<?>) response.getData();

            for (Object obj : list) {
              Map<?, ?> map = (Map<?, ?>) obj;
              String auctionId = (String) map.get("auctionId");

              // Parse giá hiện tại
              Object priceObj = map.get("currentPrice");
              double currentPrice = priceObj instanceof Number
                  ? ((Number) priceObj).doubleValue()
                  : 0.0;

              // Parse tên sản phẩm từ object item con
              Map<?, ?> itemMap = (Map<?, ?>) map.get("item");
              String name = (itemMap != null && itemMap.get("name") != null)
                  ? (String) itemMap.get("name")
                  : "Sản phẩm ẩn";

              // Tái tạo lại AuctionItem để đẩy vào TableView
              AuctionItem item = new AuctionItem(auctionId, name, "", "", "", currentPrice);
              tableData.add(item);
            }
            lblMessage.setText("Tải dữ liệu thành công lúc " + java.time.LocalTime.now());
          } else {
            lblMessage.setStyle("-fx-text-fill: #ff4c4c;");
            lblMessage.setText("Chưa có phiên đấu giá nào đang diễn ra.");
          }
        });
      } catch (Exception e) {
        Platform.runLater(() -> {
          lblMessage.setStyle("-fx-text-fill: #ff4c4c;");
          lblMessage.setText("Lỗi mạng: " + e.getMessage());
        });
      }
    }).start();
  }

  /**
   * Xử lý luồng đặt giá đấu, gửi lên Server bằng JSON.
   */
  @FXML
  private void handlePlaceBid() {
    AuctionItem selectedItem = productTable.getSelectionModel().getSelectedItem();
    if (selectedItem == null) {
      showAlert(AlertType.WARNING, "Cảnh báo", "Vui lòng chọn một sản phẩm trên bảng!");
      return;
    }

    String bidText = txtBidAmount.getText().trim();
    if (bidText.isEmpty()) {
      showAlert(AlertType.WARNING, "Cảnh báo", "Vui lòng nhập số tiền muốn đặt!");
      return;
    }

    try {
      double bidAmount = Double.parseDouble(bidText);
      if (bidAmount <= selectedItem.getEstimatedPrice()) {
        showAlert(AlertType.ERROR, "Lỗi giá", "Giá đặt phải lớn hơn giá hiện tại ($"
            + selectedItem.getEstimatedPrice() + ")!");
        return;
      }

      lblMessage.setStyle("-fx-text-fill: #00c6ff;");
      lblMessage.setText("Đang gửi yêu cầu đặt giá...");

      new Thread(() -> {
        try {
          Message request = new Message();
          request.setAction("BID");
          request.setRole("BIDDER");
          request.setAuctionId(selectedItem.getItemId()); // Gửi ID của phiên lên
          request.setBidAmount(bidAmount);

          Message response = AuctionClient.getInstance().sendRequest(request);

          Platform.runLater(() -> {
            if ("SUCCESS".equals(response.getStatus())) {
              lblMessage.setStyle("-fx-text-fill: #27ae60;");
              lblMessage.setText("Đặt giá thành công!");
              txtBidAmount.clear();
              loadData(); // Gọi lại hàm tải dữ liệu để làm mới bảng
            } else {
              lblMessage.setStyle("-fx-text-fill: #ff4c4c;");
              lblMessage.setText("Đặt giá thất bại từ phía Server.");
            }
          });
        } catch (Exception e) {
          Platform.runLater(() -> {
            lblMessage.setStyle("-fx-text-fill: #ff4c4c;");
            lblMessage.setText("Lỗi mạng: " + e.getMessage());
          });
        }
      }).start();

    } catch (NumberFormatException e) {
      showAlert(AlertType.ERROR, "Lỗi định dạng", "Số tiền không hợp lệ. Chỉ nhập số!");
    }
  }

  /**
   * Xử lý sự kiện đăng xuất và ngắt hoàn toàn kết nối.
   */
  @FXML
  private void handleLogout(ActionEvent event) {
    try {
      Parent loginRoot = FXMLLoader.load(getClass().getResource("/com/auction/login.fxml"));
      Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
      stage.setScene(new Scene(loginRoot));
      stage.centerOnScreen();
    } catch (IOException e) {
      showAlert(AlertType.ERROR, "Lỗi hệ thống", "Không thể tải màn hình đăng nhập!");
    }
  }

  private void showAlert(AlertType type, String title, String content) {
    Alert alert = new Alert(type);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(content);
    alert.showAndWait();
  }
}