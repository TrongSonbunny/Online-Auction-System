package com.auction.backend.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Quản lý khởi tạo database.
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

    String sql =
        "CREATE TABLE IF NOT EXISTS users ("
            + "user_id VARCHAR(50) PRIMARY KEY,"
            + "name VARCHAR(255) NOT NULL,"
            + "email VARCHAR(255) NOT NULL UNIQUE,"
            + "role VARCHAR(50) NOT NULL"
            + ");";

    executeSql(sql);
  }

  /**
   * Tạo bảng items.
   */
  private void createItemTable() {

    String sql =
        "CREATE TABLE IF NOT EXISTS items ("
            + "item_id VARCHAR(50) PRIMARY KEY,"
            + "name VARCHAR(255) NOT NULL,"
            + "description TEXT NOT NULL,"
            + "category VARCHAR(100) NOT NULL,"
            + "item_condition VARCHAR(100) NOT NULL,"
            + "estimated_price DOUBLE NOT NULL"
            + ");";

    executeSql(sql);
  }

  /**
   * Tạo bảng auctions.
   */
  private void createAuctionTable() {

    String sql =
        "CREATE TABLE IF NOT EXISTS auctions ("
            + "auction_id VARCHAR(50) PRIMARY KEY,"
            + "seller_id VARCHAR(50) NOT NULL,"
            + "item_id VARCHAR(50) NOT NULL,"
            + "starting_price DOUBLE NOT NULL,"
            + "current_highest_bid DOUBLE NOT NULL,"
            + "current_highest_bidder_id VARCHAR(50),"
            + "status VARCHAR(50) NOT NULL,"
            + "created_at TIMESTAMP NOT NULL,"
            + "start_time TIMESTAMP NULL,"
            + "end_time TIMESTAMP NULL,"
            + "FOREIGN KEY (seller_id)"
            + " REFERENCES users(user_id),"
            + "FOREIGN KEY (item_id)"
            + " REFERENCES items(item_id)"
            + ");";

    executeSql(sql);
  }

  /**
   * Tạo bảng bid transactions.
   */
  private void createBidTransactionTable() {

    String sql =
        "CREATE TABLE IF NOT EXISTS bid_transactions ("
            + "transaction_id VARCHAR(50)"
            + " PRIMARY KEY,"
            + "bidder_id VARCHAR(50) NOT NULL,"
            + "auction_id VARCHAR(50) NOT NULL,"
            + "bid_amount DOUBLE NOT NULL,"
            + "created_at TIMESTAMP NOT NULL,"
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
  private void executeSql(String sql) {

    try (
        Connection connection =
            MySqlConnection.getConnection();

        Statement statement =
            connection.createStatement()) {

      statement.execute(sql);

    } catch (SQLException exception) {

      exception.printStackTrace();
    }
  }
}