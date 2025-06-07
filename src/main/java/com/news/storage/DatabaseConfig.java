package com.news.storage;

import com.news.ConfigLoader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {
    public static Connection getConnection() throws SQLException {
        String url = ConfigLoader.getDBUrl();
        String user = ConfigLoader.getDBUser();
        String password = ConfigLoader.getDBPassword();
        System.out.println("Successfully connected to database!");
        assert url != null;
        return DriverManager.getConnection(url, user, password);
    }
}
