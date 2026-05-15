/**
 * Module định nghĩa cấu hình cho ứng dụng hệ thống đấu giá.
 * Khai báo các module con cần thiết và quyền truy cập cho JavaFX, SQL và Gson.
 */
module com.auction {
  // 1. Các module giao diện JavaFX cốt lõi
  requires javafx.controls;
  requires javafx.fxml;

  // 2. Cho phép sử dụng JDBC/SQLite (Dành cho tầng Backend/Database)
  requires java.sql;

  // 3. Cho phép sử dụng thư viện Gson để xử lý chuỗi JSON
  requires com.google.gson;

  // 4. Cấp quyền cho JavaFX load các file FXML và tạo Controller
  opens com.auction to javafx.fxml;
  opens com.auction.controllers to javafx.fxml;

  // 5. MỞ CÁC GÓI MODEL CHO JAVAFX (Đổ dữ liệu lên bảng) VÀ GSON (Parse JSON
  // Realtime)
  // Đã xóa com.auction.models vì đây là package rỗng chỉ chứa thư mục con
  opens com.auction.models.item to javafx.base, com.google.gson;
  opens com.auction.models.user to javafx.base, com.google.gson;
  opens com.auction.models.user.permission to javafx.base, com.google.gson; // Bổ sung để parse User Role/Permission
  opens com.auction.models.auction to javafx.base, com.google.gson;
  opens com.auction.models.bid to javafx.base, com.google.gson;
  opens com.auction.models.payment to javafx.base, com.google.gson;

  // Mở gói network để parse các đối tượng ClientMessage từ JSON
  opens com.auction.network to javafx.base, com.google.gson;

  // 6. Xuất các gói để các module bên ngoài có thể tương tác (nếu có)
  exports com.auction;
  exports com.auction.models.item;
  exports com.auction.models.user;
  exports com.auction.models.auction;
  exports com.auction.models.bid;
  exports com.auction.models.payment;
  exports com.auction.network;
}