package com.news.storage.impl;

import com.news.model.Article;
import com.news.storage.ArticleRepository;
import com.news.storage.StorageException;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcArticleRepository implements ArticleRepository {
    private final Connection connection;

    public JdbcArticleRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void save(Article article) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO articles (title, content, url, author, region, published_at) VALUES (?, ?, ?, ?, ?, ?) ON CONFLICT DO NOTHING")) {
            stmt.setString(1, article.getTitle());
            stmt.setString(2, article.getContent());
            stmt.setString(3, article.getUrl());
            stmt.setString(4, article.getAuthor());
            stmt.setString(5, article.getRegion());
            LocalDateTime published = article.getPublishedAt();
            if (published != null) {
                stmt.setTimestamp(6, Timestamp.valueOf(published));
            } else {
                stmt.setTimestamp(6, null); // crutch
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new StorageException("Failed to save article", e);
        }
    }

    @Override
    public void saveAll(List<Article> articles) {
        for (Article article : articles)
            save(article);
    }

    @Override
    public Optional<Article> findByUrl(String url) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT * FROM articles WHERE url = ?")) {
            stmt.setString(1, url);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Article article = Article.builder()
                        .title(rs.getString("title"))
                        .publishedAt(rs.getTimestamp("published_at").toLocalDateTime())
                        .build();
                return Optional.of(article);
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new StorageException("Failed to find article by url", e);
        }
    }

    @Override
    public List<Article> findAll() {
        List<Article> articles = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT * FROM articles");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                articles.add(Article.builder()
                        .title(rs.getString("title"))
                        .url(rs.getString("url"))
                        .publishedAt(rs.getTimestamp("published_at").toLocalDateTime())
                        .build());
            }
            return articles;
        } catch (SQLException e) {
            throw new StorageException("Failed to fetch all articles", e);
        }
    }

    @Override
    public void deleteOlderThanDays(int days) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM articles WHERE published_at < NOW() - INTERVAL '? days'")) {
            stmt.setInt(1, days);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new StorageException("Failed to delete old articles", e);
        }
    }

//    @Override
//    public int getOrCreateTagId(String tagName) throws SQLException {
//        PreparedStatement ps = connection.prepareStatement("SELECT id FROM tags WHERE name = ?");
//        ps.setString(1, tagName);
//        ResultSet rs = ps.executeQuery();
//        if (rs.next()) return rs.getInt("id");
//
//        ps = connection.prepareStatement("INSERT INTO tags (name) VALUES (?) RETURNING id");
//        ps.setString(1, tagName);
//        rs = ps.executeQuery();
//        return rs.next() ? rs.getInt("id") : -1;
//    }
//
//    @Override
//    public void linkArticleTags(int articleId, int tagId) throws SQLException {
//        PreparedStatement ps = connection.prepareStatement("INSERT INTO article_tags (article_id, tag_id) VALUES (?, ?) ON CONFLICT DO NOTHING");
//        ps.setInt(1, articleId);
//        ps.setInt(2, tagId);
//        ps.executeUpdate();
//    }
}
