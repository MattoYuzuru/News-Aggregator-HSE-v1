package com.news.storage.impl;

import com.news.model.Article;
import com.news.model.ArticleStatus;
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
                "INSERT INTO articles (title, content, url, author, region, published_at, source_name, language, status) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT DO NOTHING")) {
            stmt.setString(1, article.getTitle());
            stmt.setString(2, article.getContent());
            stmt.setString(3, article.getUrl());
            stmt.setString(4, article.getAuthor());
            stmt.setString(5, article.getRegion());

            LocalDateTime published = article.getPublishedAt();
            if (published != null) {
                stmt.setTimestamp(6, Timestamp.valueOf(published));
            } else {
                stmt.setNull(6, Types.TIMESTAMP);
            }

            stmt.setString(7, article.getSourceName());
            stmt.setString(8, article.getLanguage());

            stmt.setString(9, article.getStatus() != null ? article.getStatus().name() : ArticleStatus.RAW.name());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new StorageException("Failed to save article", e);
        }
    }

    @Override
    public void saveAll(List<Article> articles) {
        for (Article article : articles) {
            save(article);
        }
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
                        .content(rs.getString("content"))
                        .url(rs.getString("url"))
                        .author(rs.getString("author"))
                        .region(rs.getString("region"))
                        .publishedAt(rs.getTimestamp("published_at") != null
                                ? rs.getTimestamp("published_at").toLocalDateTime()
                                : null)
                        .sourceName(rs.getString("source_name"))
                        .language(rs.getString("language"))
                        .status(ArticleStatus.valueOf(rs.getString("status")))
                        .build();
                return Optional.of(article);
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new StorageException("Failed to find article by url", e);
        }
    }

    @Override
    public Optional<Integer> findIdByUrl(String url) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT id FROM articles WHERE url = ?")) {
            stmt.setString(1, url);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(rs.getInt("id"));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new StorageException("Failed to find article ID by URL", e);
        }
    }

    @Override
    public List<Article> findAll() {
        List<Article> articles = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM articles");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                articles.add(Article.builder()
                        .title(rs.getString("title"))
                        .content(rs.getString("content"))
                        .url(rs.getString("url"))
                        .author(rs.getString("author"))
                        .region(rs.getString("region"))
                        .publishedAt(rs.getTimestamp("published_at") != null
                                ? rs.getTimestamp("published_at").toLocalDateTime()
                                : null)
                        .sourceName(rs.getString("source_name"))
                        .language(rs.getString("language"))
                        .status(ArticleStatus.valueOf(rs.getString("status")))
                        .build());
            }

            return articles;
        } catch (SQLException e) {
            throw new StorageException("Failed to fetch all articles", e);
        }
    }

    @Override
    public List<Article> findByStatus(ArticleStatus status) {
        List<Article> articles = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM articles WHERE status = ?")) {
            stmt.setString(1, status.name());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                articles.add(Article.builder()
                        .title(rs.getString("title"))
                        .content(rs.getString("content"))
                        .url(rs.getString("url"))
                        .author(rs.getString("author"))
                        .region(rs.getString("region"))
                        .publishedAt(rs.getTimestamp("published_at") != null
                                ? rs.getTimestamp("published_at").toLocalDateTime()
                                : null)
                        .sourceName(rs.getString("source_name"))
                        .language(rs.getString("language"))
                        .status(ArticleStatus.valueOf(rs.getString("status")))
                        .build());
            }

            return articles;
        } catch (SQLException e) {
            throw new StorageException("Failed to fetch articles by status", e);
        }
    }

    @Override
    public void update(Article article) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "UPDATE articles SET title = ?, content = ?, author = ?, region = ?, published_at = ?, " +
                        "source_name = ?, language = ?, status = ?, summary = ? WHERE url = ?")) {
            stmt.setString(1, article.getTitle());
            stmt.setString(2, article.getContent());
            stmt.setString(3, article.getAuthor());
            stmt.setString(4, article.getRegion());

            LocalDateTime published = article.getPublishedAt();
            if (published != null) {
                stmt.setTimestamp(5, Timestamp.valueOf(published));
            } else {
                stmt.setNull(5, Types.TIMESTAMP);
            }

            stmt.setString(6, article.getSourceName());
            stmt.setString(7, article.getLanguage());
            stmt.setString(8, article.getStatus() != null ? article.getStatus().name() : ArticleStatus.RAW.name());
            stmt.setString(9, article.getSummary());
            stmt.setString(10, article.getUrl());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new StorageException("Failed to update article", e);
        }
    }

    @Override
    public void deleteOlderThanDays(int days) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM articles WHERE published_at < (CURRENT_TIMESTAMP - (? * INTERVAL '1 day'))")) {
            stmt.setInt(1, days);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new StorageException("Failed to delete old articles", e);
        }
    }
}
