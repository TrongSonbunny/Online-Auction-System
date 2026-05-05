package com.auction.controllers;

import com.auction.App;
import com.auction.exceptions.AuctionClosedException;
import com.auction.exceptions.InvalidBidException;
import com.auction.factory.ItemFactory;
import com.auction.manager.AuctionManager;
import com.auction.models.Item;
import com.auction.services.AuctionService;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Controller chính xử lý giao diện bảng danh sách sản phẩm đấu giá.
 */
public class PrimaryController implements Initializable {

  @FXML
  private TableView<Item> productTable;

  @FXML
  private TableColumn<Item, String> colId;

  @FXML
  private TableColumn<Item, String> colName;

  @FXML
  private TableColumn<Item, Double> colPrice;

  // Thêm ô nhập giá tiền từ FXML
  @FXML
  private TextField txtBidAmount;

  private AuctionService auctionService;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    auctionService = new AuctionService();
    AuctionManager manager = AuctionManager.getInstance();

    if (manager.getAuctionItems().isEmpty()) {
      Item laptop = ItemFactory.createItem("electronics", "Macbook Pro M3", 1500.0, 12);
      Item phone = ItemFactory.createItem("electronics", "iPhone 15 Pro", 999.0, 12);
      manager.addItem(laptop);
      manager.addItem(phone);
    }

    colId.setCellValueFactory(new PropertyValueFactory<>("id"));
    colName.setCellValueFactory(new PropertyValueFactory<>("name"));
    colPrice.setCellValueFactory(new PropertyValueFactory<>("startingPrice"));

    ObservableList<Item> tableData = FXCollections.observableArrayList(manager.getAuctionItems());
    productTable.setItems(tableData);
  }

  /**
   * Xử lý sự kiện khi người dùng nhấn nút "Đặt giá".
   * Bao gồm xử lý lỗi dữ liệu và các ngoại lệ nghiệp vụ.
   */
  @FXML
  private void handlePlaceBid() {
    // 1. Lỗi dữ liệu: Kiểm tra xem đã chọn sản phẩm chưa
    Item selectedItem = productTable.getSelectionModel().getSelectedItem();
    if (selectedItem == null) {
      showAlert(AlertType.WARNING, "Cảnh báo", "Vui lòng chọn một sản phẩm để đặt giá!");
      return;
    }

    String bidText = txtBidAmount.getText();
    // Lỗi dữ liệu: Ô nhập giá bị bỏ trống
    if (bidText == null || bidText.trim().isEmpty()) {
      showAlert(AlertType.WARNING, "Cảnh báo", "Vui lòng nhập số tiền muốn đặt!");
      return;
    }

    try {
      // 2. Lỗi dữ liệu: Chuyển đổi chuỗi sang số (sẽ ném lỗi nếu nhập chữ)
      double bidAmount = Double.parseDouble(bidText);

      // Giả lập trạng thái đóng/mở phiên đấu giá (có thể lấy từ model trong thực tế)
      boolean isAuctionClosed = false;

      // 3. Gọi Service để xử lý đặt giá. Nếu có lỗi nghiệp vụ, nó sẽ ném Exception
      auctionService.placeBid(selectedItem, bidAmount, isAuctionClosed);

      // Nếu code chạy đến đây nghĩa là không có lỗi (đặt giá thành công)
      showAlert(AlertType.INFORMATION, "Thành công", "Bạn đã đặt giá thành công!");

      // Làm mới lại bảng để hiển thị giá mới và xóa ô nhập liệu
      productTable.refresh();
      txtBidAmount.clear();

    } catch (NumberFormatException e) {
      // Bắt lỗi khi người dùng nhập chữ (VD: "abc") vào ô giá tiền
      showAlert(AlertType.ERROR, "Lỗi dữ liệu", "Số tiền không hợp lệ. Vui lòng chỉ nhập số!");
    } catch (InvalidBidException | AuctionClosedException e) {
      // Bắt lỗi nghiệp vụ: Giá thấp hơn hiện tại HOẶC phiên đã đóng
      showAlert(AlertType.ERROR, "Lỗi đặt giá", e.getMessage());
    }
  }

  /**
   * Phương thức hỗ trợ hiển thị hộp thoại thông báo (Alert).
   *
   * @param type    Loại hộp thoại (Lỗi, Cảnh báo, Thông tin)
   * @param title   Tiêu đề của hộp thoại
   * @param content Nội dung thông báo
   */
  private void showAlert(AlertType type, String title, String content) {
    Alert alert = new Alert(type);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(content);
    alert.showAndWait();
  }
}