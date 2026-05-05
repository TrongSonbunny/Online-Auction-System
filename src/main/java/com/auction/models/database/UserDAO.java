package com.auction.models.database;

import com.auction.models.user.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO cho User.
 */
public class UserDAO {

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
                // ⚠️ Vì User là abstract → mapping ở service layer
                throw new UnsupportedOperationException(
                        "Cần map User subclass ở service layer (role-based mapping)");
            }
        }

        return null;
    }

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