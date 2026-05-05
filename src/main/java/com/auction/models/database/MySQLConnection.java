package com.auction.models.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Quản lý kết nối MySQL.
 */
public final class MySQLConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/auction_db?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "123456";

    private MySQLConnection() {
    }

    /**
     * Lấy connection mới.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}