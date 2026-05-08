package com.auction.models.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.auction.models.user.User;

/**
 * Data Access Object (DAO) cho đối tượng User.
 * Quản lý các thao tác lưu trữ và truy xuất thông tin người dùng từ database.
 */
public class UserDao {

  /**
   * Lưu thông tin người dùng mới vào cơ sở dữ liệu.
   *
   * @param user đối tượng người dùng cần lưu
   * @throws SQLException nếu có lỗi SQL hoặc vi phạm ràng buộc dữ liệu (trùng ID)
   */
  public void save(User user) throws SQLException {
    if (user == null) {
      throw new IllegalArgumentException("User không được null");
    }

    String sql = """
        INSERT INTO users (user_id, name, email, password_hash, role)
        VALUES (?, ?, ?, ?, ?)
        """;

    try (Connection conn = DatabaseManager.getInstance().getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, user.getUserId());
      stmt.setString(2, user.getName());
      stmt.setString(3, user.getEmail());
      stmt.setString(4, user.getPasswordHash());
      stmt.setString(5, user.getRole());

      stmt.executeUpdate();

    } catch (SQLIntegrityConstraintViolationException e) {
      throw new SQLException("User đã tồn tại với ID: " + user.getUserId(), e);
    }
  }

  /**
   * Tìm kiếm thông tin người dùng theo ID.
   * Lưu ý: Do User là lớp abstract, việc khởi tạo cụ thể sẽ được xử lý ở lớp trên.
   *
   * @param userId ID người dùng cần tìm
   * @return đối tượng User (hiện tại ném exception để xử lý mapping sau)
   * @throws SQLException nếu có lỗi truy vấn
   * @throws UnsupportedOperationException vì cần mapping subclass cụ thể
   */
  public User findById(String userId) throws SQLException {
    if (userId == null || userId.isBlank()) {
      throw new IllegalArgumentException("userId không hợp lệ");
    }

    String sql = "SELECT * FROM users WHERE user_id = ?";

    try (Connection conn = DatabaseManager.getInstance().getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, userId);
      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        // ⚠️ Vì User là abstract -> mapping ở service layer dựa trên role
        throw new UnsupportedOperationException(
            "Cần map User subclass ở service layer (role-based mapping)");
      }
    }

    return null;
  }

  /**
   * Lấy danh sách ID của tất cả người dùng hiện có.
   *
   * @return danh sách các chuỗi ID
   * @throws SQLException nếu có lỗi truy vấn
   */
  public List<String> findAllUserIds() throws SQLException {
    List<String> ids = new ArrayList<>();
    String sql = "SELECT user_id FROM users";

    try (Connection conn = DatabaseManager.getInstance().getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {

      while (rs.next()) {
        ids.add(rs.getString("user_id"));
      }
    }

    return ids;
  }
}