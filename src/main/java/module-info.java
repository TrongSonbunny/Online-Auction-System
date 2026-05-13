/**
 * Module định nghĩa cấu hình cho ứng dụng hệ thống đấu giá.
 * Khai báo các module con cần thiết và quyền truy cập cho JavaFX, SQL và Gson.
 */
module com.auction {
	// 1. Các module giao diện JavaFX cốt lõi
	requires javafx.controls;
	requires javafx.fxml;

	// 2. Cho phép sử dụng JDBC/SQLite
	requires java.sql;

	// 3. Cho phép sử dụng thư viện Gson để xử lý JSON
	requires com.google.gson;

	// Cấp quyền cho JavaFX load các file FXML và Controller
	opens com.auction to javafx.fxml;
	opens com.auction.controllers to javafx.fxml;

	// Mở gói models cho cả JavaFX và Gson
	opens com.auction.models to javafx.base, com.google.gson;

	// FIX LỖI: Mở gói thư mục con (item) cho cả JavaFX và Gson
	opens com.auction.models.item to javafx.base, com.google.gson;

	// Xuất các gói để các module khác có thể sử dụng
	exports com.auction;
	exports com.auction.models;
	exports com.auction.models.item;
}