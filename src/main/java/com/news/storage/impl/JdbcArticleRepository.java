package com.news.storage.impl;

import com.news.model.Article;
import com.news.model.ArticleFilter;
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
    public void deleteById(Integer id) {
        try (
                PreparedStatement deleteTags = connection.prepareStatement(
                        "DELETE FROM article_tags WHERE article_id = ?"
                );
                PreparedStatement deleteArticle = connection.prepareStatement(
                        "DELETE FROM articles WHERE id = ?"
                )
        ) {
            deleteTags.setInt(1, id);
            deleteTags.executeUpdate();

            deleteArticle.setInt(1, id);
            int affectedRows = deleteArticle.executeUpdate();

            if (affectedRows == 0) {
                throw new StorageException("No article found with id: " + id, null);
            }
        } catch (SQLException e) {
            throw new StorageException("Failed to delete article by id", e);
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
                        .summary(rs.getString("summary"))
                        .build();
                return Optional.of(article);
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new StorageException("Failed to find article by url", e);
        }
    }

    @Override
    public Optional<Article> findById(Integer id) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT * FROM articles WHERE id = ?")) {
            stmt.setInt(1, id);
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
                        .summary(rs.getString("summary"))
                        .build();
                return Optional.of(article);
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new StorageException("Failed to find article by id", e);
        }
    }

    @Override
    public Optional<List<Article>> findBySubstrInContent(String substr) {
        String searchPattern = "%" + substr + "%";
        String sql = "SELECT * FROM articles WHERE articles.content ILIKE ?";
        List<Article> articles = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, searchPattern);
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
                        .summary(rs.getString("summary"))
                        .build());
            }

            return Optional.of(articles);
        } catch (SQLException e) {
            throw new StorageException("Failed to find article by substring", e);
        }
    }

    @Override
    public Optional<List<Article>> findBySubstrInTitle(String substr) {
        String searchPattern = "%" + substr + "%";
        String sql = "SELECT * FROM articles WHERE articles.title ILIKE ?";
        List<Article> articles = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, searchPattern);
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
                        .summary(rs.getString("summary"))
                        .build());
            }

            return Optional.of(articles);
        } catch (SQLException e) {
            throw new StorageException("Failed to find article by substring", e);
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
                        .summary(rs.getString("summary"))
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
                        .summary(rs.getString("summary"))
                        .build());
            }

            return articles;
        } catch (SQLException e) {
            throw new StorageException("Failed to fetch articles by status", e);
        }
    }

    // Helper method to avoid duplicate code when building Article objects
    private Article buildArticleFromResultSet(ResultSet rs) throws SQLException {
        return Article.builder()
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
                .summary(rs.getString("summary"))
                .build();
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
    public List<Article> findArticlesWithFilters(ArticleFilter filter) {
        List<Article> articles = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT DISTINCT a.* FROM articles a");

        if (filter.getTags() != null && !filter.getTags().isEmpty()) {
            sql.append(" LEFT JOIN article_tags at ON a.id = at.article_id")
                    .append(" LEFT JOIN tags t ON at.tag_id = t.id");
        }

        sql.append(" WHERE 1=1"); // always true to simplify adding ANDs

        List<Object> params = new ArrayList<>();

        if (filter.getSource() != null && !filter.getSource().equals("all")) {
            sql.append(" AND a.source_name LIKE ?");
            params.add("%" + filter.getSource() + "%");
        }

        if (filter.getStatus() != null) {
            sql.append(" AND a.status = ?");
            params.add(filter.getStatus().name());
        }

        if (filter.getLanguage() != null && !filter.getLanguage().equals("all")) {
            sql.append(" AND a.language = ?");
            params.add(filter.getLanguage());
        }

        if (filter.getAuthor() != null) {
            sql.append(" AND a.author LIKE ?");
            params.add("%" + filter.getAuthor() + "%");
        }

        if (filter.isTodayOnly()) {
            LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
            LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);
            sql.append(" AND a.published_at BETWEEN ? AND ?");
            params.add(Timestamp.valueOf(startOfDay));
            params.add(Timestamp.valueOf(endOfDay));
        } else {
            if (filter.getPublishedAfter() != null) {
                sql.append(" AND a.published_at >= ?");
                params.add(Timestamp.valueOf(filter.getPublishedAfter()));
            }

            if (filter.getPublishedBefore() != null) {
                sql.append(" AND a.published_at <= ?");
                params.add(Timestamp.valueOf(filter.getPublishedBefore()));
            }
        }

        if (filter.getTags() != null && !filter.getTags().isEmpty()) {
            sql.append(" AND t.name IN (");
            for (int i = 0; i < filter.getTags().size(); i++) {
                sql.append(i > 0 ? ", ?" : "?");
                params.add(filter.getTags().get(i));
            }
            sql.append(")");
        }

        sql.append(" ORDER BY a.").append(filter.getSortBy());
        sql.append(filter.isAscending() ? " ASC" : " DESC");

        if (filter.getLimit() != null) {
            sql.append(" LIMIT ?");
            params.add(filter.getLimit());
        }

        if (filter.getOffset() != null) {
            sql.append(" OFFSET ?");
            params.add(filter.getOffset());
        }

        try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    articles.add(buildArticleFromResultSet(rs));
                }
            }

            return articles;
        } catch (SQLException e) {
            throw new StorageException("Failed to fetch articles with filters", e);
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
