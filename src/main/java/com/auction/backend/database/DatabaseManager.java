package com.auction.backend.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Quản lý khởi tạo database SQLite.
 */
public class DatabaseManager {

  /**
   * Khởi tạo toàn bộ database.
   */
  public void initializeDatabase() {

    createUserTable();
    createItemTable();
    createAuctionTable();
    createBidTransactionTable();
  }

  /**
   * Tạo bảng users.
   */
  private void createUserTable() {

    String sql = "CREATE TABLE IF NOT EXISTS users ("
        + "user_id TEXT PRIMARY KEY,"
        + "name TEXT NOT NULL,"
        + "email TEXT NOT NULL UNIQUE,"
        + "password TEXT NOT NULL,"
        + "role TEXT NOT NULL"
        + ");";

    executeSql(sql);
  }

  /**
   * Tạo bảng items.
   */
  private void createItemTable() {

    String sql = "CREATE TABLE IF NOT EXISTS items ("
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

    String sql = "CREATE TABLE IF NOT EXISTS auctions ("
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

    String sql = "CREATE TABLE IF NOT EXISTS bid_transactions ("
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
        Connection connection = DatabaseConnection.getConnection();

        Statement statement = connection.createStatement()) {

      statement.execute(sql);

    } catch (SQLException exception) {

      exception.printStackTrace();
    }
  }
}