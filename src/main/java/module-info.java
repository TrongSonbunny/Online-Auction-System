/**
 * Module định nghĩa cấu hình cho ứng dụng hệ thống đấu giá.
 * Khai báo các module con cần thiết và quyền truy cập cho JavaFX.
 */
module com.auction {
	requires javafx.controls;
	requires javafx.fxml;

	opens com.auction to javafx.fxml;
	// --- THÊM DÒNG NÀY ĐỂ JAVAFX ĐỌC ĐƯỢC CONTROLLER MỚI ---
	opens com.auction.controllers to javafx.fxml;

	exports com.auction;

	// --- THÊM 2 DÒNG NÀY ĐỂ FIX LỖI BẢNG ---
	// Cho phép JavaFX (cụ thể là thư viện javafx.base) truy cập vào
	// thư mục models để đọc dữ liệu thông qua Reflection.
	opens com.auction.models to javafx.base;

	exports com.auction.models;
}