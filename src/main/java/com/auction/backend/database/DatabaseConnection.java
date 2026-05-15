package com.auction.backend.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Quản lý kết nối SQLite.
 */
public final class DatabaseConnection {

  private static final String URL = "jdbc:sqlite:auction_system.db";

  /**
   * Private constructor.
   */
  private DatabaseConnection() {
  }

  /**
   * Tạo database connection an toàn.
   *
   * @return Connection object
   * @throws SQLException nếu kết nối thất bại
   */
  public static Connection getConnection() throws SQLException {
    // 1. Ép Java nạp Driver của SQLite vào bộ nhớ trước khi gọi
    try {
      Class.forName("org.sqlite.JDBC");
    } catch (ClassNotFoundException e) {
      throw new SQLException("Không tìm thấy Driver SQLite!", e);
    }

    Connection connection = DriverManager.getConnection(URL);
    enableForeignKeys(connection);
    return connection;
  }

  /**
   * Kích hoạt khóa ngoại (Foreign Keys) cho SQLite.
   *
   * @param connection Kết nối cơ sở dữ liệu
   * @throws SQLException nếu thực thi thất bại
   */
  private static void enableForeignKeys(Connection connection) throws SQLException {
    try (Statement statement = connection.createStatement()) {
      statement.execute("PRAGMA foreign_keys = ON");
    }
  }
}