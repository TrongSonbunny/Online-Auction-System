package com.auction;

import com.auction.factory.ItemFactory;
import com.auction.manager.AuctionManager;
import com.auction.models.Item;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Controller chính xử lý giao diện bảng danh sách sản phẩm đấu giá.
 * Kế thừa Initializable để khởi tạo dữ liệu ngay khi giao diện được tải lên.
 */
public class PrimaryController implements Initializable {

  // Khai báo các thành phần giao diện (Tên biến phải TRÙNG KHỚP với fx:id trong
  // file FXML)
  @FXML
  private TableView<Item> productTable;

  @FXML
  private TableColumn<Item, String> colId;

  @FXML
  private TableColumn<Item, String> colName;

  @FXML
  private TableColumn<Item, Double> colPrice;

  /**
   * Phương thức này tự động chạy khi cửa sổ (Scene) vừa được mở lên.
   * Dùng để thiết lập dữ liệu ban đầu cho bảng.
   *
   * @param location  Đường dẫn (URL) dùng để giải quyết các đường dẫn tương đối
   * @param resources Các tài nguyên được bản địa hóa
   */
  @Override
  public void initialize(URL location, ResourceBundle resources) {

    // --- TẠO DỮ LIỆU MẪU (MODEL) ---
    AuctionManager manager = AuctionManager.getInstance();
    if (manager.getAuctionItems().isEmpty()) { // Tránh thêm trùng dữ liệu nếu mở lại Scene
      Item laptop = ItemFactory.createItem("electronics", "Macbook Pro M3", 1500.0, 12);
      Item phone = ItemFactory.createItem("electronics", "iPhone 15 Pro", 999.0, 12);
      manager.addItem(laptop);
      manager.addItem(phone);
    }

    // --- LIÊN KẾT DỮ LIỆU VÀO CỘT (CONTROLLER kết nối MODEL với VIEW) ---
    // Các thuộc tính "id", "name", "startingPrice" phải khớp với tên biến trong lớp
    // Item
    colId.setCellValueFactory(new PropertyValueFactory<>("id"));
    colName.setCellValueFactory(new PropertyValueFactory<>("name"));
    colPrice.setCellValueFactory(new PropertyValueFactory<>("startingPrice"));

    // Lấy danh sách từ Manager, chuyển thành dạng ObservableList của JavaFX
    ObservableList<Item> tableData = FXCollections.observableArrayList(manager.getAuctionItems());

    // Đẩy toàn bộ dữ liệu lên bảng
    productTable.setItems(tableData);
  }
}