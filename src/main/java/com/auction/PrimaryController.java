package com.auction;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.fxml.Initializable;
import java.net.URL;
import java.util.ResourceBundle;

import com.auction.factory.ItemFactory;
import com.auction.manager.AuctionManager;
import com.auction.models.Item;

// Thêm "implements Initializable" để chạy code ngay khi giao diện vừa bật lên
public class PrimaryController implements Initializable {

    // 1. Khai báo các thành phần giao diện (Tên biến phải TRÙNG KHỚP với fx:id ở
    // Bước 1)
    @FXML
    private TableView<Item> productTable;

    @FXML
    private TableColumn<Item, String> colId;

    @FXML
    private TableColumn<Item, String> colName;

    @FXML
    private TableColumn<Item, Double> colPrice;

    // 2. Hàm này tự động chạy khi cửa sổ vừa mở lên
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // --- TẠO DỮ LIỆU MẪU (MODEL) ---
        AuctionManager manager = AuctionManager.getInstance();
        if (manager.getAuctionItems().isEmpty()) { // Tránh thêm trùng nếu mở lại
            Item laptop = ItemFactory.createItem("electronics", "Macbook Pro M3", 1500.0, 12);
            Item phone = ItemFactory.createItem("electronics", "iPhone 15 Pro", 999.0, 12);
            manager.addItem(laptop);
            manager.addItem(phone);
        }

        // --- LIÊN KẾT DỮ LIỆU VÀO CỘT (CONTROLLER kết nối MODEL với VIEW) ---
        // Chữ "id", "name", "startingPrice" phải TRÙNG với tên biến trong file
        // Item.java
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("startingPrice"));

        // Lấy danh sách từ Manager, chuyển thành dạng ObservableList của JavaFX
        ObservableList<Item> tableData = FXCollections.observableArrayList(manager.getAuctionItems());

        // Đẩy toàn bộ dữ liệu lên bảng
        productTable.setItems(tableData);
    }
}