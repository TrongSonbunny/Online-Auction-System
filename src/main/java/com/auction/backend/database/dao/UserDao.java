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
	 * @param user     user cần lưu
	 * @param password mật khẩu
	 */
	public void saveUser(User user, String password) {
		validateUser(user);
		validatePassword(password);

		String sql = "INSERT INTO users (user_id, name, email, password, role) "
				+ "VALUES (?, ?, ?, ?, ?)";

		try (Connection connection = DatabaseConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setString(1, user.getUserId());
			statement.setString(2, user.getName());
			statement.setString(3, user.getEmail());
			statement.setString(4, password);
			statement.setString(5, user.getRole().name());

			statement.executeUpdate();

		} catch (SQLException exception) {
			exception.printStackTrace();
			throw new AuctionException("Email này đã được sử dụng hoặc lỗi CSDL!");
		}
	}

	/**
	 * Tìm user theo id.
	 *
	 * @param userId Mã user
	 * @return Đối tượng User
	 */
	public User findById(String userId) {
		String sql = "SELECT user_id, name, email, role FROM users WHERE user_id = ?";

		try (Connection connection = DatabaseConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setString(1, userId);
			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					return mapUser(resultSet);
				}
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
			throw new AuctionException("Không thể tìm user theo id.");
		}
		return null;
	}

	/**
	 * Tìm user theo email.
	 *
	 * @param email Email của user
	 * @return Đối tượng User
	 */
	public User findByEmail(String email) {
		String sql = "SELECT user_id, name, email, role FROM users WHERE email = ?";

		try (Connection connection = DatabaseConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setString(1, email);
			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					return mapUser(resultSet);
				}
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
			throw new AuctionException("Không thể tìm user theo email.");
		}
		return null;
	}

	/**
	 * Kiểm tra email đã tồn tại chưa.
	 *
	 * @param email Email cần kiểm tra
	 * @return true nếu tồn tại
	 */
	public boolean existsByEmail(String email) {
		String sql = "SELECT COUNT(*) FROM users WHERE email = ?";

		try (Connection connection = DatabaseConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setString(1, email);
			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					return resultSet.getInt(1) > 0;
				}
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
			throw new AuctionException("Lỗi kiểm tra email trong CSDL.");
		}
		return false;
	}

	/**
	 * Kiểm tra password.
	 *
	 * @param email    Email của user
	 * @param password Password nhập vào
	 * @return true nếu password chính xác
	 */
	public boolean checkPassword(String email, String password) {
		String sql = "SELECT password FROM users WHERE email = ?";

		try (Connection connection = DatabaseConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setString(1, email);
			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					return resultSet.getString("password").equals(password);
				}
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
			throw new AuctionException("Lỗi kiểm tra password trong CSDL.");
		}
		return false;
	}

	/**
	 * Xóa user.
	 *
	 * @param userId Mã user cần xóa
	 */
	public void deleteUser(String userId) {
		String sql = "DELETE FROM users WHERE user_id = ?";

		try (Connection connection = DatabaseConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setString(1, userId);
			statement.executeUpdate();
		} catch (SQLException exception) {
			exception.printStackTrace();
			throw new AuctionException("Không thể xóa user.");
		}
	}

	/**
	 * Chuyển đổi ResultSet thành User.
	 *
	 * @param resultSet Kết quả truy vấn
	 * @return Đối tượng User
	 * @throws SQLException Nếu có lỗi CSDL
	 */
	private User mapUser(ResultSet resultSet) throws SQLException {
		String userId = resultSet.getString("user_id");
		String name = resultSet.getString("name");
		String email = resultSet.getString("email");
		UserRole role = UserRole.valueOf(resultSet.getString("role"));

		return UserFactory.createUserWithId(userId, role, name, email);
	}

	/**
	 * Xác thực đối tượng User.
	 *
	 * @param user User cần kiểm tra
	 */
	private void validateUser(User user) {
		if (user == null) {
			throw new AuctionException("User không được null.");
		}
	}

	/**
	 * Xác thực Password.
	 *
	 * @param password Password cần kiểm tra
	 */
	private void validatePassword(String password) {
		if (password == null || password.isBlank()) {
			throw new AuctionException("Password không hợp lệ.");
		}
	}
}