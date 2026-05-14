package com.auction.backend.database.dao;

import com.auction.backend.database.DatabaseConnection;
import com.auction.exceptions.AuctionException;
import com.auction.models.user.User;
import com.auction.models.user.UserFactory;
import com.auction.models.user.UserRole;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO xử lý user database SQLite.
 */
public class UserDao {

  /**
   * Lưu user vào database.
   *
   * @param user user cần lưu
   * @param password mật khẩu
   */
  public void saveUser(
      User user,
      String password) {

    validateUser(user);
    validatePassword(password);

    String sql =
        "INSERT INTO users "
            + "(user_id, name, email, password, role) "
            + "VALUES (?, ?, ?, ?, ?)";

    try (
        Connection connection =
            DatabaseConnection.getConnection();

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
          password);

      statement.setString(
          5,
          user.getRole().name());

      statement.executeUpdate();

    } catch (SQLException exception) {

      throw new AuctionException(
          "Không thể lưu user.",
          exception);
    }
  }

  /**
   * Tìm user theo id.
   *
   * @param userId mã user
   * @return user hoặc null
   */
  public User findById(
      String userId) {

    String sql =
        "SELECT user_id, name, email, role "
            + "FROM users "
            + "WHERE user_id = ?";

    try (
        Connection connection =
            DatabaseConnection.getConnection();

        PreparedStatement statement =
            connection.prepareStatement(sql)) {

      statement.setString(
          1,
          userId);

      try (ResultSet resultSet =
          statement.executeQuery()) {

        if (resultSet.next()) {
          return mapUser(resultSet);
        }
      }

    } catch (SQLException exception) {

      throw new AuctionException(
          "Không thể tìm user theo id.",
          exception);
    }

    return null;
  }

  /**
   * Tìm user theo email.
   *
   * @param email email
   * @return user hoặc null
   */
  public User findByEmail(
      String email) {

    String sql =
        "SELECT user_id, name, email, role "
            + "FROM users "
            + "WHERE email = ?";

    try (
        Connection connection =
            DatabaseConnection.getConnection();

        PreparedStatement statement =
            connection.prepareStatement(sql)) {

      statement.setString(
          1,
          email);

      try (ResultSet resultSet =
          statement.executeQuery()) {

        if (resultSet.next()) {
          return mapUser(resultSet);
        }
      }

    } catch (SQLException exception) {

      throw new AuctionException(
          "Không thể tìm user theo email.",
          exception);
    }

    return null;
  }

  /**
   * Kiểm tra email đã tồn tại chưa.
   *
   * @param email email
   * @return true nếu tồn tại
   */
  public boolean existsByEmail(
      String email) {

    String sql =
        "SELECT COUNT(*) "
            + "FROM users "
            + "WHERE email = ?";

    try (
        Connection connection =
            DatabaseConnection.getConnection();

        PreparedStatement statement =
            connection.prepareStatement(sql)) {

      statement.setString(
          1,
          email);

      try (ResultSet resultSet =
          statement.executeQuery()) {

        if (resultSet.next()) {
          return resultSet.getInt(1) > 0;
        }
      }

    } catch (SQLException exception) {

      throw new AuctionException(
          "Không thể kiểm tra email.",
          exception);
    }

    return false;
  }

  /**
   * Kiểm tra password.
   *
   * @param email email
   * @param password mật khẩu
   * @return true nếu đúng
   */
  public boolean checkPassword(
      String email,
      String password) {

    String sql =
        "SELECT password "
            + "FROM users "
            + "WHERE email = ?";

    try (
        Connection connection =
            DatabaseConnection.getConnection();

        PreparedStatement statement =
            connection.prepareStatement(sql)) {

      statement.setString(
          1,
          email);

      try (ResultSet resultSet =
          statement.executeQuery()) {

        if (resultSet.next()) {
          return resultSet
              .getString("password")
              .equals(password);
        }
      }

    } catch (SQLException exception) {

      throw new AuctionException(
          "Không thể kiểm tra password.",
          exception);
    }

    return false;
  }

  /**
   * Xóa user.
   *
   * @param userId mã user
   */
  public void deleteUser(
      String userId) {

    String sql =
        "DELETE FROM users "
            + "WHERE user_id = ?";

    try (
        Connection connection =
            DatabaseConnection.getConnection();

        PreparedStatement statement =
            connection.prepareStatement(sql)) {

      statement.setString(
          1,
          userId);

      statement.executeUpdate();

    } catch (SQLException exception) {

      throw new AuctionException(
          "Không thể xóa user.",
          exception);
    }
  }

  /**
   * Map ResultSet thành User.
   *
   * @param resultSet dữ liệu database
   * @return user
   * @throws SQLException nếu đọc dữ liệu lỗi
   */
  private User mapUser(
      ResultSet resultSet)
      throws SQLException {

    String userId =
        resultSet.getString("user_id");

    String name =
        resultSet.getString("name");

    String email =
        resultSet.getString("email");

    UserRole role =
        UserRole.valueOf(
            resultSet.getString("role"));

    return UserFactory.createUserWithId(
        userId,
        role,
        name,
        email);
  }

  /**
   * Validate user.
   *
   * @param user user
   */
  private void validateUser(
      User user) {

    if (user == null) {
      throw new AuctionException(
          "User không được null.");
    }
  }

  /**
   * Validate password.
   *
   * @param password mật khẩu
   */
  private void validatePassword(
      String password) {

    if (password == null || password.isBlank()) {
      throw new AuctionException(
          "Password không hợp lệ.");
    }
  }
}