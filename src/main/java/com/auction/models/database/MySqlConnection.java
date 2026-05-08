package com.auction.models.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Lớp quản lý cấu hình và thiết lập kết nối tới cơ sở dữ liệu MySQL.
 */
public final class MySqlConnection {

  private static final String URL =
      "jdbc:mysql://localhost:3306/auction_db?useSSL=false&serverTimezone=UTC";
  private static final String USER = "root";
  private static final String PASSWORD = "123456";

  /**
   * Constructor riêng tư để ngăn chặn việc khởi tạo lớp tiện ích.
   */
  private MySqlConnection() {}

  /**
   * Thiết lập và trả về một kết nối mới tới MySQL Server.
   *
   * @return đối tượng Connection tới cơ sở dữ liệu
   * @throws SQLException nếu thông tin đăng nhập sai hoặc server không phản hồi
   */
  public static Connection getConnection() throws SQLException {
    return DriverManager.getConnection(URL, USER, PASSWORD);
  }
}