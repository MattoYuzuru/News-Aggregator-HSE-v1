package com.news.storage;

import com.news.model.Article;
import com.news.storage.impl.JdbcArticleRepository;
import com.news.storage.impl.JdbcArticleTagLinker;
import com.news.storage.impl.JdbcTagRepository;
import lombok.Getter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Getter
public class DatabaseService {
    private final Connection connection;
    private final ArticleRepository articleRepository;
    private final TagRepository tagRepository;
    private final ArticleTagLinker articleTagLinker;

    public DatabaseService() throws SQLException {
        this.connection = DatabaseConfig.getConnection();
        this.connection.setAutoCommit(false);

        this.articleRepository = new JdbcArticleRepository(connection);
        this.tagRepository = new JdbcTagRepository(connection);
        this.articleTagLinker = new JdbcArticleTagLinker(connection);
    }

    public void saveArticle(Article article) {
        try {
            Optional<Article> existingArticle = articleRepository.findByUrl(article.getUrl());

            if (existingArticle.isPresent()) {
                Article updated = mergeArticles(existingArticle.get(), article);
                articleRepository.update(updated);
            } else {
                articleRepository.save(article);
            }

            if (article.getTags() != null && !article.getTags().isEmpty()) {
                Optional<Long> articleId = articleRepository.findIdByUrl(article.getUrl());
                if (articleId.isPresent()) {
                    for (String tag : article.getTags()) {
                        int tagId = tagRepository.getOrCreateTagId(tag);
                        articleTagLinker.linkArticleTags(articleId.get(), tagId);
                    }
                }
            }

            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                throw new StorageException("Failed to rollback transaction", rollbackEx);
            }
            throw new StorageException("Database error when saving article", e);
        }
    }

    /**
     * Merge articles by preserving data from different processing stages
     * maybe it's a wrong place to be in -> fix!!
     */
    private Article mergeArticles(Article existing, Article newArticle) {
        Article.ArticleBuilder builder = Article.builder()
                .title(newArticle.getTitle() != null ? newArticle.getTitle() : existing.getTitle())
                .content(newArticle.getContent() != null ? newArticle.getContent() : existing.getContent())
                .url(existing.getUrl())
                .author(newArticle.getAuthor() != null ? newArticle.getAuthor() : existing.getAuthor())
                .region(newArticle.getRegion() != null ? newArticle.getRegion() : existing.getRegion())
                .publishedAt(newArticle.getPublishedAt() != null ? newArticle.getPublishedAt() : existing.getPublishedAt())
                .sourceName(newArticle.getSourceName() != null ? newArticle.getSourceName() : existing.getSourceName())
                .language(newArticle.getLanguage() != null ? newArticle.getLanguage() : existing.getLanguage())
                .rating(newArticle.getRating() != null ? newArticle.getRating() : existing.getRating());

        if (newArticle.getSummary() != null) {
            builder.summary(newArticle.getSummary());
        } else if (existing.getSummary() != null) {
            builder.summary(existing.getSummary());
        }

        if (existing.getTags() != null && newArticle.getTags() != null) {
            List<String> mergedTags = existing.getTags();
            for (String tag : newArticle.getTags()) {
                if (!mergedTags.contains(tag)) {
                    mergedTags.add(tag);
                }
            }
            builder.tags(mergedTags);
        } else if (newArticle.getTags() != null) {
            builder.tags(newArticle.getTags());
        } else if (existing.getTags() != null) {
            builder.tags(existing.getTags());
        }

        if (newArticle.getStatus().ordinal() > existing.getStatus().ordinal()) {
            builder.status(newArticle.getStatus());
        } else {
            builder.status(existing.getStatus());
        }

        if (newArticle.getRating() != null) {
            builder.rating(newArticle.getRating());
        } else if (existing.getRating() != null) {
            builder.rating((existing.getRating()));
        }

        return builder.build();
    }

    public void cleanupDatabase() throws SQLException {
        try {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "TRUNCATE TABLE articles, article_tags, tags RESTART IDENTITY CASCADE;"
            )) {
                stmt.executeUpdate();
            }
            connection.commit();

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                throw new StorageException("Failed to rollback transaction", rollbackEx);
            }
            throw new StorageException("Database error during cleanup", e);
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new StorageException("Failed to close database connection", e);
        }
    }
}