package com.news.storage;

import com.news.model.Article;

import java.sql.*;
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
                "INSERT INTO articles (url, published_at) VALUES (?, ?) ON CONFLICT (url) DO NOTHING")) {
            stmt.setString(1, article.getTitle());
            stmt.setString(2, article.getUrl());
            stmt.setTimestamp(3, Timestamp.valueOf(article.getPublishedAt()));
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
}
