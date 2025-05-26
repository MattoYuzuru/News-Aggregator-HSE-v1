package com.news.storage.util;

import com.news.model.Article;
import com.news.model.ArticleStatus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class to map SQL ResultSet to Article objects
 */
public class ArticleResultSetMapper {
    public static Article mapRow(ResultSet rs) throws SQLException {
        return Article.builder()
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
                .build();
    }

    public static List<Article> mapRows(ResultSet rs) throws SQLException {
        List<Article> articles = new ArrayList<>();
        while (rs.next()) {
            articles.add(mapRow(rs));
        }
        return articles;
    }
}