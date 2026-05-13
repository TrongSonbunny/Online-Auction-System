package com.auction.network;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Lớp quản lý cơ sở dữ liệu SQLite cho Backend của hệ thống.
 */
public class DatabaseManager {

  private static final String DB_URL = "jdbc:sqlite:auction.db";

  /**
   * Khởi tạo cơ sở dữ liệu và tạo bảng nếu chưa tồn tại.
   */
  public static void initialize() {
    String createTableSql = "CREATE TABLE IF NOT EXISTS users ("
        + "username TEXT PRIMARY KEY, "
        + "password TEXT NOT NULL"
        + ");";

    try (Connection conn = DriverManager.getConnection(DB_URL);
        Statement stmt = conn.createStatement()) {
      stmt.execute(createTableSql);
      System.out.println("Cơ sở dữ liệu SQLite đã sẵn sàng!");
    } catch (SQLException e) {
      System.err.println("Lỗi khởi tạo DB: " + e.getMessage());
    }
  }

  /**
   * Đăng ký tài khoản người dùng mới.
   *
   * @param username Tên đăng nhập.
   * @param password Mật khẩu.
   * @return true nếu đăng ký thành công, false nếu tài khoản đã tồn tại.
   */
  public static boolean registerUser(String username, String password) {
    String insertSql = "INSERT INTO users(username, password) VALUES(?, ?)";
    try (Connection conn = DriverManager.getConnection(DB_URL);
        PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
      pstmt.setString(1, username);
      pstmt.setString(2, password);
      pstmt.executeUpdate();
      return true;
    } catch (SQLException e) {
      return false; // Lỗi do trùng khóa chính (username đã tồn tại)
    }
  }

  /**
   * Xác thực thông tin đăng nhập của người dùng.
   *
   * @param username Tên đăng nhập.
   * @param password Mật khẩu.
   * @return true nếu đúng tài khoản và mật khẩu, ngược lại false.
   */
  public static boolean authenticateUser(String username, String password) {
    String querySql = "SELECT password FROM users WHERE username = ?";
    try (Connection conn = DriverManager.getConnection(DB_URL);
        PreparedStatement pstmt = conn.prepareStatement(querySql)) {
      pstmt.setString(1, username);
      ResultSet rs = pstmt.executeQuery();
      if (rs.next()) {
        String dbPassword = rs.getString("password");
        return dbPassword.equals(password);
      }
    } catch (SQLException e) {
      System.err.println("Lỗi xác thực: " + e.getMessage());
    }
    return false;
  }
}