package com.auction.backend.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Quản lý kết nối SQLite.
 */
public final class DatabaseConnection {

  private static final String URL =
      "jdbc:sqlite:auction_system.db";

  /**
   * Private constructor.
   */
  private DatabaseConnection() {
  }

  /**
   * Tạo database connection.
   *
   * @return Connection object
   * @throws SQLException nếu kết nối thất bại
   */
  public static Connection getConnection()
      throws SQLException {

    Connection connection =
        DriverManager.getConnection(URL);

    enableForeignKeys(connection);

    return connection;
  }

  /**
   * Bật foreign key cho SQLite.
   *
   * @param connection kết nối database
   * @throws SQLException nếu bật foreign key thất bại
   */
  private static void enableForeignKeys(
      Connection connection)
      throws SQLException {

    try (Statement statement =
        connection.createStatement()) {

      statement.execute(
          "PRAGMA foreign_keys = ON");
    }
  }
}