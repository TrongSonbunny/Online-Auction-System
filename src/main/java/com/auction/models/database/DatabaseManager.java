package com.auction.models.database;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Facade cho database layer.
 */
public final class DatabaseManager {

    private static final DatabaseManager INSTANCE = new DatabaseManager();

    private DatabaseManager() {
    }

    public static DatabaseManager getInstance() {
        return INSTANCE;
    }

    public Connection getConnection() throws SQLException {
        return MySQLConnection.getConnection();
    }
}