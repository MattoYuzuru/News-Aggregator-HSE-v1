package com.news.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {
    public static Connection getConnection() throws SQLException {
        String url = "jdbc:postgresql://localhost:5429/news";
        String user = "keyko";
        String password = "qaplTY123&sdf";
        System.out.println("Success!");
        return DriverManager.getConnection(url, user, password);
    }
}
