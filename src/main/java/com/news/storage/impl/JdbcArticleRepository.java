package com.news.storage.impl;

import com.news.model.Article;
import com.news.model.ArticleFilter;
import com.news.model.ArticleStatus;
import com.news.storage.ArticleRepository;
import com.news.storage.StorageException;
import com.news.storage.util.ArticleResultSetMapper;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
                "INSERT INTO articles (title, content, url, author, region, published_at, source_name, language, status, summary, image_url) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT DO NOTHING")) {
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
            stmt.setString(10, article.getSummary());
            stmt.setString(11, article.getImageUrl());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new StorageException("Failed to save article", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM articles WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting article with id: " + id, e);
        }
    }

    @Override
    public Optional<Article> findByUrl(String url) {
        String sql = "SELECT * FROM articles WHERE url = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, url);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(ArticleResultSetMapper.mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding article by URL: " + url, e);
        }
    }

    @Override
    public Optional<Article> findById(Long id) {
        String sql = "SELECT * FROM articles WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(ArticleResultSetMapper.mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding article by ID: " + id, e);
        }
    }

    @Override
    public Optional<List<Article>> findBySubstrInContent(String substr) {
        String sql = "SELECT * FROM articles WHERE content LIKE ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "%" + substr + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                List<Article> articles = ArticleResultSetMapper.mapRows(rs);
                return articles.isEmpty() ? Optional.empty() : Optional.of(articles);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding articles by content substring: " + substr, e);
        }
    }

    @Override
    public Optional<List<Article>> findBySubstrInTitle(String substr) {
        String sql = "SELECT * FROM articles WHERE title LIKE ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "%" + substr + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                List<Article> articles = ArticleResultSetMapper.mapRows(rs);
                return articles.isEmpty() ? Optional.empty() : Optional.of(articles);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding articles by title substring: " + substr, e);
        }
    }

    @Override
    public Optional<Long> findIdByUrl(String url) {
        String sql = "SELECT id FROM articles WHERE url = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, url);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rs.getLong("id"));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding article ID by URL: " + url, e);
        }
    }

    @Override
    public List<Article> findAll() {
        String sql = "SELECT * FROM articles";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return ArticleResultSetMapper.mapRows(rs);
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all articles", e);
        }
    }

    @Override
    public List<Article> findByStatus(ArticleStatus status) {
        String sql = "SELECT * FROM articles WHERE status = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            try (ResultSet rs = stmt.executeQuery()) {
                return ArticleResultSetMapper.mapRows(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding articles by status: " + status, e);
        }
    }

    @Override
    public void update(Article article) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "UPDATE articles SET title = ?, content = ?, author = ?, region = ?, published_at = ?, " +
                        "source_name = ?, language = ?, status = ?, summary = ?, image_url = ? WHERE url = ?")) {
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
            stmt.setString(10, article.getImageUrl());
            stmt.setString(11, article.getUrl());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new StorageException("Failed to update article", e);
        }
    }

    @Override
    public List<Article> findArticlesWithFilters(ArticleFilter filter) {
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();

        // Check if we need to join with tags table
        boolean needsTagJoin = filter.getTags() != null && !filter.getTags().isEmpty();

        if (needsTagJoin) {
            sql.append("SELECT DISTINCT a.* FROM articles a ");
            sql.append("JOIN article_tags at ON a.id = at.article_id ");
            sql.append("JOIN tags t ON at.tag_id = t.id ");
            sql.append("WHERE 1=1");
        } else {
            sql.append("SELECT * FROM articles WHERE 1=1");
        }

        if (filter.getSource() != null) {
            if (needsTagJoin) {
                sql.append(" AND a.source_name = ?");
            } else {
                sql.append(" AND source_name = ?");
            }
            params.add(filter.getSource());
        }

        if (filter.getStatus() != null) {
            if (needsTagJoin) {
                sql.append(" AND a.status = ?");
            } else {
                sql.append(" AND status = ?");
            }
            params.add(filter.getStatus().name());
        }

        if (filter.getLanguage() != null) {
            if (needsTagJoin) {
                sql.append(" AND a.language = ?");
            } else {
                sql.append(" AND language = ?");
            }
            params.add(filter.getLanguage());
        }

        if (filter.getAuthor() != null) {
            if (needsTagJoin) {
                sql.append(" AND a.author = ?");
            } else {
                sql.append(" AND author = ?");
            }
            params.add(filter.getAuthor());
        }

        if (filter.getPublishedAfter() != null) {
            if (needsTagJoin) {
                sql.append(" AND a.published_at >= ?");
            } else {
                sql.append(" AND published_at >= ?");
            }
            params.add(filter.getPublishedAfter());
        }

        if (filter.getPublishedBefore() != null) {
            if (needsTagJoin) {
                sql.append(" AND a.published_at <= ?");
            } else {
                sql.append(" AND published_at <= ?");
            }
            params.add(filter.getPublishedBefore());
        }

        if (filter.isTodayOnly()) {
            LocalDate today = LocalDate.now();
            if (needsTagJoin) {
                sql.append(" AND a.published_at BETWEEN ? AND ?");
            } else {
                sql.append(" AND published_at BETWEEN ? AND ?");
            }
            params.add(today.atStartOfDay());
            params.add(today.atTime(LocalTime.MAX));
        }

        if (filter.getTags() != null && !filter.getTags().isEmpty()) {
            sql.append(" AND t.name IN (");
            for (int i = 0; i < filter.getTags().size(); i++) {
                sql.append(i > 0 ? ", ?" : "?");
                params.add(filter.getTags().get(i));
            }
            sql.append(")");
        }

        if (filter.getSortBy() != null) {
            if (needsTagJoin) {
                sql.append(" ORDER BY a.").append(filter.getSortBy());
            } else {
                sql.append(" ORDER BY ").append(filter.getSortBy());
            }
            sql.append(filter.isAscending() ? " ASC" : " DESC");
        }

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
                setParameter(stmt, i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                return ArticleResultSetMapper.mapRows(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error finding articles with filters", e);
        }
    }

    @Override
    public void deleteOlderThanDays(int days) {
        String sql = "DELETE FROM articles WHERE published_at < ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, LocalDateTime.now().minusDays(days));
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting articles older than " + days + " days", e);
        }
    }

    private void setParameter(PreparedStatement stmt, int index, Object value) throws SQLException {
        switch (value) {
            case null -> stmt.setNull(index, Types.NULL);
            case String s -> stmt.setString(index, s);
            case Long l -> stmt.setLong(index, l);
            case Integer i -> stmt.setInt(index, i);
            case Boolean b -> stmt.setBoolean(index, b);
            default -> stmt.setObject(index, value);
        }
    }
}