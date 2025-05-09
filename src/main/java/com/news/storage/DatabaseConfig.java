package com.news.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(
                    "jdbc:postgresql://localhost:5429/news", "keyko", "qaplTY123&sdf");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to DB: ", e);
        }
    }
}
