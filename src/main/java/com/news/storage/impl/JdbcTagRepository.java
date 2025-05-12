package com.news.storage.impl;

import com.news.storage.TagRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcTagRepository implements TagRepository {
    private final Connection connection;

    public JdbcTagRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public int getOrCreateTagId(String tagName) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT id FROM tags WHERE name = ?")) {
            ps.setString(1, tagName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }

        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO tags (name) VALUES (?) RETURNING id")) {
            ps.setString(1, tagName);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt("id") : -1;
        }
    }
}
