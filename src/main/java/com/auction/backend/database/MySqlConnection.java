package com.auction.backend.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Quản lý kết nối MySQL.
 */
public final class MySqlConnection {

  private static final String URL =
      "jdbc:mysql://localhost:3306/auction_system";

  private static final String USERNAME =
      "root";

  private static final String PASSWORD =
      "root";

  /**
   * Private constructor.
   */
  private MySqlConnection() {
  }

  /**
   * Tạo database connection.
   *
   * @return Connection object
   * @throws SQLException nếu kết nối thất bại
   */
  public static Connection getConnection()
      throws SQLException {

    return DriverManager.getConnection(
        URL,
        USERNAME,
        PASSWORD);
  }
}