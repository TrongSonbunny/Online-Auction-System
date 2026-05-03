module com.auction {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.auction to javafx.fxml;

    exports com.auction;

    // --- THÊM 2 DÒNG NÀY ĐỂ FIX LỖI BẢNG ---
    // Cho phép JavaFX truy cập vào thư mục models để lấy dữ liệu
    opens com.auction.models to javafx.base;

    exports com.auction.models;
}