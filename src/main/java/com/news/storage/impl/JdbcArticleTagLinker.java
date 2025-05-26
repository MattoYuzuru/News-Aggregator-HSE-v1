package com.news.storage.impl;

import com.news.storage.ArticleTagLinker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class JdbcArticleTagLinker implements ArticleTagLinker {
    private final Connection connection;

    public JdbcArticleTagLinker(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void linkArticleTags(long articleId, int tagId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO article_tags (article_id, tag_id) VALUES (?, ?) ON CONFLICT DO NOTHING")) {
            ps.setLong(1, articleId);
            ps.setInt(2, tagId);
            ps.executeUpdate();
        }
    }
}
