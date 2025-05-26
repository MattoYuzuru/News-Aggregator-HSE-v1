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

    /**
     * Maps a single row from a ResultSet to an Article object
     *
     * @param rs The ResultSet to map
     * @return The mapped Article object
     * @throws SQLException If a database access error occurs
     */
    public static Article mapRow(ResultSet rs) throws SQLException {
        return Article.builder()
                .id(rs.getLong("id"))
                .title(rs.getString("title"))
                .url(rs.getString("url"))
                .summary(rs.getString("summary"))
                .content(rs.getString("content"))
                .region(rs.getString("region"))
                .tags(parseTagsFromString(rs.getString("tags")))
                .publishedAt(rs.getObject("published_at", LocalDateTime.class))
                .author(rs.getString("author"))
                .sourceName(rs.getString("source_name"))
                .imageUrl(rs.getString("image_url"))
                .language(rs.getString("language"))
                .status(ArticleStatus.valueOf(rs.getString("status")))
                .build();
    }

    /**
     * Maps multiple rows from a ResultSet to a List of Article objects
     *
     * @param rs The ResultSet to map
     * @return A List of mapped Article objects
     * @throws SQLException If a database access error occurs
     */
    public static List<Article> mapRows(ResultSet rs) throws SQLException {
        List<Article> articles = new ArrayList<>();
        while (rs.next()) {
            articles.add(mapRow(rs));
        }
        return articles;
    }

    /**
     * Parses a comma-separated string of tags into a List
     *
     * @param tagsString Comma-separated string of tags
     * @return List of tags
     */
    private static List<String> parseTagsFromString(String tagsString) {
        if (tagsString == null || tagsString.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(tagsString.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .collect(Collectors.toList());
    }
}