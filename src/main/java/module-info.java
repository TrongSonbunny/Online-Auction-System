/**
 * Module định nghĩa cấu hình cho ứng dụng hệ thống đấu giá.
 * Đảm bảo các quyền truy cập cho JavaFX, SQL và thư viện JSON.
 */
module com.auction {
  // 1. Các module giao diện JavaFX cốt lõi
  requires javafx.controls;
  requires javafx.fxml;
  requires javafx.graphics;

  // 2. Thư viện hỗ trợ Backend và Database
  requires java.sql;

  // 3. Thư viện xử lý chuỗi JSON
  requires com.google.gson;

  // 4. Thư viện Logging cho hệ thống mạng
  requires org.slf4j;

  // 5. Cấp quyền cho JavaFX load các file FXML và tạo Controller
  // Package com.auction PHẢI chứa tệp App.java
  opens com.auction to javafx.fxml, javafx.graphics;
  opens com.auction.controllers to javafx.fxml;

  // 6. Mở gói network để Gson parse được các gói tin JSON
  // (ClientMessage/ServerMessage)
  opens com.auction.network to com.google.gson;

  // 7. Xuất các gói ra bên ngoài
  exports com.auction;
}