package com.auction.backend.database.dao;

import com.auction.backend.database.MySqlConnection;
import com.auction.exceptions.AuctionException;
import com.auction.models.user.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * DAO thực hiện INSERT/DELETE user trong bảng {@code users}.
 *
 * <p>Cùng cơ chế kết nối với {@link AuctionDao}: mỗi thao tác dùng
 * try-with-resources, lỗi SQL in ra stderr.
 */
public class UserDao {

  /**
   * Lưu user vào database.
   *
   * @param user user cần lưu
   */
  public void saveUser(User user) {

    validateUser(user);

    String sql =
        "INSERT INTO users "
            + "(user_id, name, email, role) "
            + "VALUES (?, ?, ?, ?)";

    try (
        Connection connection =
            MySqlConnection.getConnection();

        PreparedStatement statement =
            connection.prepareStatement(sql)) {

      statement.setString(
          1,
          user.getUserId());

      statement.setString(
          2,
          user.getName());

      statement.setString(
          3,
          user.getEmail());

      statement.setString(
          4,
          user.getRole().name());

      statement.executeUpdate();

    } catch (SQLException exception) {

      exception.printStackTrace();
    }
  }

  /**
   * Xóa user.
   *
   * @param userId mã user
   */
  public void deleteUser(String userId) {

    String sql =
        "DELETE FROM users WHERE user_id = ?";

    try (
        Connection connection =
            MySqlConnection.getConnection();

        PreparedStatement statement =
            connection.prepareStatement(sql)) {

      statement.setString(1, userId);

      statement.executeUpdate();

    } catch (SQLException exception) {

      exception.printStackTrace();
    }
  }

  /**
   * Validate user.
   *
   * @param user user
   */
  private void validateUser(User user) {

    if (user == null) {

      throw new AuctionException(
          "User không được null.");
    }
  }
}