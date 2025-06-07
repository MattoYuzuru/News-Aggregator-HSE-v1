package com.news.storage.util;

import com.news.model.Article;
import com.news.model.ArticleStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ArticleResultSetMapper {

    public static Article mapRow(ResultSet rs, Connection connection) throws SQLException {
        Article.ArticleBuilder builder = Article.builder()
                .id(rs.getLong("id"))
                .title(rs.getString("title"))
                .url(rs.getString("url"))
                .summary(rs.getString("summary"))
                .content(rs.getString("content"))
                .region(rs.getString("region"))
                .publishedAt(rs.getTimestamp("published_at") != null ?
                        rs.getTimestamp("published_at").toLocalDateTime() : null)
                .author(rs.getString("author"))
                .sourceName(rs.getString("source_name"))
                .imageUrl(rs.getString("image_url"))
                .language(rs.getString("language"))
                .status(ArticleStatus.valueOf(rs.getString("status")))
                .rating(rs.getObject("rating") != null ? rs.getInt("rating") : null);

        if (connection != null) {
            builder.tags(loadTagsForArticle(rs.getLong("id"), connection));
        }

        return builder.build();
    }

    public static List<Article> mapRows(ResultSet rs, Connection connection) throws SQLException {
        List<Article> articles = new ArrayList<>();
        while (rs.next()) {
            articles.add(mapRow(rs, connection));
        }
        return articles;
    }

    private static List<String> loadTagsForArticle(Long articleId, Connection connection) throws SQLException {
        List<String> tags = new ArrayList<>();
        String sql = "SELECT t.name FROM tags t " +
                "JOIN article_tags at ON t.id = at.tag_id " +
                "WHERE at.article_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, articleId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tags.add(rs.getString("name"));
                }
            }
        }
        return tags;
    }
}