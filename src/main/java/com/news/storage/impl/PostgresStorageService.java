package com.news.storage.impl;

import com.news.model.Article;
import com.news.storage.StorageService;

import java.sql.*;
import java.util.List;

public class PostgresStorageService implements StorageService {

    private final Connection connection;

    public PostgresStorageService(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void saveArticle(Article article) {
        if (articleExists(article.getUrl())) return;

        try {
            connection.setAutoCommit(false);

            String insertArticle = "INSERT INTO articles (title, url, published_at, content) VALUES (?, ?, ?, ?) RETURNING id";
            PreparedStatement ps = connection.prepareStatement(insertArticle);
            ps.setString(1, article.getTitle());
            ps.setString(2, article.getUrl());
            ps.setTimestamp(3, Timestamp.valueOf(article.getPublishedAt()));
            ps.setString(4, article.getContent());

            ResultSet rs = ps.executeQuery();
            int articleId = rs.next() ? rs.getInt("id") : -1;

            for (String tag : article.getTags()) {
                int tagId = getOrCreateTagId(tag);
                linkArticleTag(articleId, tagId);
            }

            connection.commit();
        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    @Override
    public void saveArticles(List<Article> articles) {
        for (Article article : articles) {
            saveArticle(article);
        }
    }

    @Override
    public boolean articleExists(String url) {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT 1 FROM articles WHERE url = ?");
            ps.setString(1, url);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private int getOrCreateTagId(String tag) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("SELECT id FROM tags WHERE name = ?");
        ps.setString(1, tag);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return rs.getInt("id");

        ps = connection.prepareStatement("INSERT INTO tags (name) VALUES (?) RETURNING id");
        ps.setString(1, tag);
        rs = ps.executeQuery();
        return rs.next() ? rs.getInt("id") : -1;
    }

    private void linkArticleTag(int articleId, int tagId) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("INSERT INTO article_tags (article_id, tag_id) VALUES (?, ?) ON CONFLICT DO NOTHING");
        ps.setInt(1, articleId);
        ps.setInt(2, tagId);
        ps.executeUpdate();
    }
}
