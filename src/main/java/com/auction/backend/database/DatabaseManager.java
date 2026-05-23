package com.auction.backend.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Quản lý khởi tạo database SQLite.
 */
public class DatabaseManager {
  private static final String DEFAULT_ADMIN_ID = "ADMIN";
  private static final String DEFAULT_ADMIN_NAME = "ADMIN";
  private static final String DEFAULT_ADMIN_EMAIL = "admin@auction.local";
  private static final String DEFAULT_ADMIN_PASSWORD = "123456789";
  private static final String DEFAULT_ADMIN_ROLE = "ADMIN";

  /**
   * Khởi tạo toàn bộ database.
   */
  public void initializeDatabase() {
    createUserTable();
    createOnlyOneAdminIndex();
    createDefaultAdminAccount();
    createItemTable();
    createAuctionTable();
    createBidTransactionTable();
  }

  /**
   * Tạo bảng users.
   */
  private void createUserTable() {
    String sql =
        "CREATE TABLE IF NOT EXISTS users ("
            + "user_id VARCHAR(50) PRIMARY KEY,"
            + "name VARCHAR(255) NOT NULL,"
            + "email VARCHAR(255) NOT NULL UNIQUE,"
            + "password VARCHAR(255) NOT NULL,"
            + "role VARCHAR(50) NOT NULL"
            + ");";

    executeSql(sql);
  }

  /**
   * Tạo ràng buộc để hệ thống chỉ có tối đa một tài khoản ADMIN.
   *
   * <p>SQLite chỉ áp dụng unique index này cho các dòng có role là ADMIN.
   * Các role khác như SELLER và BIDDER không bị ảnh hưởng.
   */
  private void createOnlyOneAdminIndex() {
    String sql =
        "CREATE UNIQUE INDEX IF NOT EXISTS idx_only_one_admin "
            + "ON users(role) "
            + "WHERE role = 'ADMIN';";

    executeSql(sql);
  }

  /**
   * Tạo tài khoản admin mặc định nếu chưa tồn tại.
   *
   * <p>Tài khoản đăng nhập mặc định:
   * <ul>
   * <li>username: ADMIN
   * <li>password: 123456789
   * </ul>
   *
   * <p>Trong database vẫn lưu email hợp lệ là {@code admin@auction.local}
   * để không phá validate email của model User.
   */
  private void createDefaultAdminAccount() {
    String sql =
        "INSERT OR IGNORE INTO users "
            + "(user_id, name, email, password, role) "
            + "VALUES ('"
            + DEFAULT_ADMIN_ID
            + "', '"
            + DEFAULT_ADMIN_NAME
            + "', '"
            + DEFAULT_ADMIN_EMAIL
            + "', '"
            + DEFAULT_ADMIN_PASSWORD
            + "', '"
            + DEFAULT_ADMIN_ROLE
            + "');";

    executeSql(sql);
  }

  /**
   * Tạo bảng items.
   */
  private void createItemTable() {
    String sql =
        "CREATE TABLE IF NOT EXISTS items ("
            + "item_id TEXT PRIMARY KEY,"
            + "name TEXT NOT NULL,"
            + "description TEXT NOT NULL,"
            + "category TEXT NOT NULL,"
            + "item_condition TEXT NOT NULL,"
            + "estimated_price REAL NOT NULL"
            + ");";

    executeSql(sql);
  }

  /**
   * Tạo bảng auctions.
   */
  private void createAuctionTable() {
    String sql =
        "CREATE TABLE IF NOT EXISTS auctions ("
            + "auction_id TEXT PRIMARY KEY,"
            + "seller_id TEXT NOT NULL,"
            + "item_id TEXT NOT NULL,"
            + "starting_price REAL NOT NULL,"
            + "current_highest_bid REAL NOT NULL,"
            + "current_highest_bidder_id TEXT,"
            + "status TEXT NOT NULL,"
            + "created_at TEXT NOT NULL,"
            + "start_time TEXT,"
            + "end_time TEXT,"
            + "FOREIGN KEY (seller_id)"
            + " REFERENCES users(user_id),"
            + "FOREIGN KEY (item_id)"
            + " REFERENCES items(item_id),"
            + "FOREIGN KEY (current_highest_bidder_id)"
            + " REFERENCES users(user_id)"
            + ");";

    executeSql(sql);
  }

  /**
   * Tạo bảng bid transactions.
   */
  private void createBidTransactionTable() {
    String sql =
        "CREATE TABLE IF NOT EXISTS bid_transactions ("
            + "transaction_id TEXT PRIMARY KEY,"
            + "bidder_id TEXT NOT NULL,"
            + "auction_id TEXT NOT NULL,"
            + "bid_amount REAL NOT NULL,"
            + "created_at TEXT NOT NULL,"
            + "FOREIGN KEY (bidder_id)"
            + " REFERENCES users(user_id),"
            + "FOREIGN KEY (auction_id)"
            + " REFERENCES auctions(auction_id)"
            + ");";

    executeSql(sql);
  }

  /**
   * Thực thi SQL.
   *
   * @param sql câu lệnh SQL
   */
  private void executeSql(
      String sql) {
    try (
        Connection connection =
            DatabaseConnection.getConnection();
        Statement statement =
            connection.createStatement()) {

      statement.execute(sql);
    } catch (SQLException exception) {
      exception.printStackTrace();
    }
  }
}