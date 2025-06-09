package com.news.storage.impl;

import com.news.storage.inter.TagRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

    public static List<String> parseTags(String tagsCommaSeparated) {
        if (tagsCommaSeparated == null || tagsCommaSeparated.isEmpty()) return List.of();
        return Arrays.stream(tagsCommaSeparated.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .collect(Collectors.toList());
    }

}
