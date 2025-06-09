package com.news.storage.impl;

import com.news.model.ArticleStatus;
import com.news.storage.inter.ArticleStatsRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class JdbcArticleStatsRepository implements ArticleStatsRepository {
    private final Connection connection;

    public JdbcArticleStatsRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public long countAllArticles() {
        String sql = "SELECT COUNT(*) FROM articles";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error counting articles", e);
        }
    }

    @Override
    public long countByStatus(ArticleStatus status) {
        String sql = "SELECT COUNT(*) FROM articles WHERE status = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting articles by status: " + status, e);
        }
    }

    @Override
    public Map<String, Long> countBySource() {
        String sql = "SELECT source_name, COUNT(*) as count FROM articles WHERE source_name IS NOT NULL GROUP BY source_name ORDER BY count DESC";
        Map<String, Long> sourceCounts = new LinkedHashMap<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                sourceCounts.put(rs.getString("source_name"), rs.getLong("count"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting articles by source", e);
        }

        return sourceCounts;
    }

    @Override
    public Map<String, Long> countByLanguage() {
        String sql = "SELECT language, COUNT(*) as count FROM articles WHERE language IS NOT NULL GROUP BY language ORDER BY count DESC";
        Map<String, Long> languageCounts = new LinkedHashMap<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                languageCounts.put(rs.getString("language"), rs.getLong("count"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting articles by language", e);
        }

        return languageCounts;
    }

    @Override
    public Map<String, Map<String, Long>> countBySourceAndStatus() {
        String sql = "SELECT source_name, status, COUNT(*) as count " +
                "FROM articles " +
                "WHERE source_name IS NOT NULL " +
                "GROUP BY source_name, status " +
                "ORDER BY source_name, status";

        Map<String, Map<String, Long>> sourceStatusCounts = new LinkedHashMap<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String source = rs.getString("source_name");
                String status = rs.getString("status");
                long count = rs.getLong("count");

                sourceStatusCounts.computeIfAbsent(source, k -> new LinkedHashMap<>())
                        .put(status, count);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting articles by source and status", e);
        }

        return sourceStatusCounts;
    }

    @Override
    public Map<String, Long> getDateRangeStats() {
        String sql = """
                SELECT 
                    CASE 
                        WHEN published_at >= NOW() - INTERVAL '1 day' THEN 'Today'
                        WHEN published_at >= NOW() - INTERVAL '7 days' THEN 'This Week'
                        WHEN published_at >= NOW() - INTERVAL '30 days' THEN 'This Month'
                        WHEN published_at >= NOW() - INTERVAL '365 days' THEN 'This Year'
                        ELSE 'Older'
                    END as time_period,
                    COUNT(*) as count
                FROM articles 
                WHERE published_at IS NOT NULL
                GROUP BY 
                    CASE 
                        WHEN published_at >= NOW() - INTERVAL '1 day' THEN 'Today'
                        WHEN published_at >= NOW() - INTERVAL '7 days' THEN 'This Week'
                        WHEN published_at >= NOW() - INTERVAL '30 days' THEN 'This Month'
                        WHEN published_at >= NOW() - INTERVAL '365 days' THEN 'This Year'
                        ELSE 'Older'
                    END
                ORDER BY 
                    CASE 
                        WHEN CASE 
                            WHEN published_at >= NOW() - INTERVAL '1 day' THEN 'Today'
                            WHEN published_at >= NOW() - INTERVAL '7 days' THEN 'This Week'
                            WHEN published_at >= NOW() - INTERVAL '30 days' THEN 'This Month'
                            WHEN published_at >= NOW() - INTERVAL '365 days' THEN 'This Year'
                            ELSE 'Older'
                        END = 'Today' THEN 1
                        WHEN CASE 
                            WHEN published_at >= NOW() - INTERVAL '1 day' THEN 'Today'
                            WHEN published_at >= NOW() - INTERVAL '7 days' THEN 'This Week'
                            WHEN published_at >= NOW() - INTERVAL '30 days' THEN 'This Month'
                            WHEN published_at >= NOW() - INTERVAL '365 days' THEN 'This Year'
                            ELSE 'Older'
                        END = 'This Week' THEN 2
                        WHEN CASE 
                            WHEN published_at >= NOW() - INTERVAL '1 day' THEN 'Today'
                            WHEN published_at >= NOW() - INTERVAL '7 days' THEN 'This Week'
                            WHEN published_at >= NOW() - INTERVAL '30 days' THEN 'This Month'
                            WHEN published_at >= NOW() - INTERVAL '365 days' THEN 'This Year'
                            ELSE 'Older'
                        END = 'This Month' THEN 3
                        WHEN CASE 
                            WHEN published_at >= NOW() - INTERVAL '1 day' THEN 'Today'
                            WHEN published_at >= NOW() - INTERVAL '7 days' THEN 'This Week'
                            WHEN published_at >= NOW() - INTERVAL '30 days' THEN 'This Month'
                            WHEN published_at >= NOW() - INTERVAL '365 days' THEN 'This Year'
                            ELSE 'Older'
                        END = 'This Year' THEN 4
                        ELSE 5
                    END
                """;

        Map<String, Long> dateStats = new LinkedHashMap<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                dateStats.put(rs.getString("time_period"), rs.getLong("count"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting date range statistics", e);
        }

        return dateStats;
    }

    @Override
    public List<String> getTopAuthors(int limit) {
        String sql = "SELECT author, COUNT(*) as count FROM articles WHERE author IS NOT NULL GROUP BY author ORDER BY count DESC LIMIT ?";
        List<String> topAuthors = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    topAuthors.add(rs.getString("author") + " (" + rs.getLong("count") + " articles)");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting top authors", e);
        }

        return topAuthors;
    }

    @Override
    public Map<String, Long> getTopTags(int limit) {
        String sql = "SELECT t.name, COUNT(*) as count " +
                "FROM tags t " +
                "JOIN article_tags at ON t.id = at.tag_id " +
                "GROUP BY t.name " +
                "ORDER BY count DESC " +
                "LIMIT ?";

        Map<String, Long> topTags = new LinkedHashMap<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    topTags.put(rs.getString("name"), rs.getLong("count"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting top tags", e);
        }

        return topTags;
    }

    @Override
    public List<String> getTopRatedArticles(int limit) {
        String sql = "SELECT title, rating, source_name, published_at " +
                "FROM articles " +
                "WHERE rating IS NOT NULL AND rating > 0 " +
                "ORDER BY rating DESC, published_at DESC " +
                "LIMIT ?";

        List<String> topRatedArticles = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String title = rs.getString("title");
                    double rating = rs.getDouble("rating");
                    String source = rs.getString("source_name");
                    LocalDateTime publishedAt = rs.getTimestamp("published_at") != null ?
                            rs.getTimestamp("published_at").toLocalDateTime() : null;

                    topRatedArticles.add(String.format("⭐ %.1f - %s (%s, %s)",
                            rating, title, source != null ? source : "Unknown",
                            publishedAt != null ? publishedAt.toString() : "No date"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting top rated articles", e);
        }

        return topRatedArticles;
    }
}