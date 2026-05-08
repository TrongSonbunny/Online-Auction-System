package com.auction.models.database;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Facade cho database layer.
 * Cung cấp một điểm truy cập duy nhất để lấy kết nối cơ sở dữ liệu.
 */
public final class DatabaseManager {

  private static final DatabaseManager INSTANCE = new DatabaseManager();

  /**
   * Constructor riêng tư để ngăn chặn việc khởi tạo từ bên ngoài.
   */
  private DatabaseManager() {}

  /**
   * Lấy thực thể duy nhất của DatabaseManager.
   *
   * @return thực thể DatabaseManager
   */
  public static DatabaseManager getInstance() {
    return INSTANCE;
  }

  /**
   * Lấy kết nối tới cơ sở dữ liệu MySQL thông qua MySQLConnection.
   *
   * @return đối tượng Connection
   * @throws SQLException nếu không thể thiết lập kết nối
   */
  public Connection getConnection() throws SQLException {
    return MySqlConnection.getConnection();
  }
}