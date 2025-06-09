package com.news.storage;

import com.news.model.Article;
import com.news.model.ArticleStatus;
import com.news.storage.inter.ArticleRepository;
import com.news.storage.inter.ArticleStatsRepository;
import com.news.storage.inter.ArticleTagLinker;
import com.news.storage.inter.TagRepository;
import com.news.storage.util.StorageException;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatabaseServiceTest {

    @Mock
    private Connection connection;

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private ArticleTagLinker articleTagLinker;

    @Mock
    private ArticleStatsRepository articleStatsRepository;

    @Mock
    private PreparedStatement preparedStatement;

    private TestableDatabaseService databaseService;

    // Create a testable version that accepts dependencies
    @Getter
    private static class TestableDatabaseService {
        private final Connection connection;
        private final ArticleRepository articleRepository;
        private final TagRepository tagRepository;
        private final ArticleTagLinker articleTagLinker;
        private final ArticleStatsRepository articleStatsRepository;

        public TestableDatabaseService(Connection connection,
                                       ArticleRepository articleRepository,
                                       TagRepository tagRepository,
                                       ArticleTagLinker articleTagLinker,
                                       ArticleStatsRepository articleStatsRepository) {
            this.connection = connection;
            this.articleRepository = articleRepository;
            this.tagRepository = tagRepository;
            this.articleTagLinker = articleTagLinker;
            this.articleStatsRepository = articleStatsRepository;
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

            return builder.build();
        }

        public void cleanupDatabase() {
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

    @BeforeEach
    void setUp() {
        databaseService = new TestableDatabaseService(
                connection, articleRepository, tagRepository, articleTagLinker, articleStatsRepository
        );
    }

    @Test
    void saveArticle_NewArticle_ShouldSaveSuccessfully() throws SQLException {
        // Given
        Article article = createTestArticle();
        when(articleRepository.findByUrl(article.getUrl())).thenReturn(Optional.empty());
        when(articleRepository.findIdByUrl(article.getUrl())).thenReturn(Optional.of(1L));
        when(tagRepository.getOrCreateTagId("tech")).thenReturn(1);
        when(tagRepository.getOrCreateTagId("news")).thenReturn(2);

        // When
        databaseService.saveArticle(article);

        // Then
        verify(articleRepository).save(article);
        verify(articleRepository, never()).update(any());
        verify(tagRepository, times(2)).getOrCreateTagId(anyString());
        verify(articleTagLinker, times(2)).linkArticleTags(eq(1L), anyInt());
        verify(connection).commit();
        verify(connection, never()).rollback();
    }

    @Test
    void saveArticle_ExistingArticle_ShouldUpdateSuccessfully() throws SQLException {
        // Given
        Article existingArticle = createTestArticle();
        Article newArticle;
        newArticle = Article.builder()
                .title("Updated Title")
                .content("Updated content")
                .url(existingArticle.getUrl())
                .status(ArticleStatus.ENRICHED)
                .tags(List.of("updated-tag"))
                .build();

        when(articleRepository.findByUrl(newArticle.getUrl())).thenReturn(Optional.of(existingArticle));
        when(articleRepository.findIdByUrl(newArticle.getUrl())).thenReturn(Optional.of(1L));
        when(tagRepository.getOrCreateTagId("updated-tag")).thenReturn(1);

        // When
        databaseService.saveArticle(newArticle);

        // Then
        verify(articleRepository, never()).save(any());
        verify(articleRepository).update(argThat(article ->
                "Updated Title".equals(article.getTitle()) &&
                        "Updated content".equals(article.getContent())
        ));
        verify(connection).commit();
    }

    @Test
    void saveArticle_WithoutTags_ShouldNotLinkTags() throws SQLException {
        // Given
        Article article = createTestArticle();
        article = Article.builder()
                .title(article.getTitle())
                .content(article.getContent())
                .url(article.getUrl())
                .status(article.getStatus())
                .tags(null) // No tags
                .build();

        when(articleRepository.findByUrl(article.getUrl())).thenReturn(Optional.empty());

        // When
        databaseService.saveArticle(article);

        // Then
        verify(articleRepository).save(article);
        verify(tagRepository, never()).getOrCreateTagId(anyString());
        verify(articleTagLinker, never()).linkArticleTags(anyLong(), anyInt());
        verify(connection).commit();
    }

    @Test
    void saveArticle_DatabaseError_ShouldRollbackAndThrowException() throws SQLException {
        // Given
        Article article = createTestArticle();
        when(articleRepository.findByUrl(article.getUrl())).thenThrow(new SQLException("Database error"));

        // When & Then
        assertThrows(StorageException.class, () -> databaseService.saveArticle(article));
        verify(connection).rollback();
        verify(connection, never()).commit();
    }

    @Test
    void saveArticle_RollbackFails_ShouldThrowStorageException() throws SQLException {
        // Given
        Article article = createTestArticle();
        when(articleRepository.findByUrl(article.getUrl())).thenThrow(new SQLException("Database error"));
        doThrow(new SQLException("Rollback failed")).when(connection).rollback();

        // When & Then
        StorageException exception = assertThrows(StorageException.class, () -> databaseService.saveArticle(article));
        assertTrue(exception.getMessage().contains("Failed to rollback transaction"));
    }

    @Test
    void cleanupDatabase_Success_ShouldTruncateTablesAndCommit() throws SQLException {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // When
        databaseService.cleanupDatabase();

        // Then
        verify(connection).prepareStatement(contains("TRUNCATE TABLE"));
        verify(preparedStatement).executeUpdate();
        verify(connection).commit();
        verify(preparedStatement).close();
    }

    @Test
    void cleanupDatabase_DatabaseError_ShouldRollbackAndThrowException() throws SQLException {
        // Given
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenThrow(new SQLException("Cleanup failed"));

        // When & Then
        assertThrows(StorageException.class, () -> databaseService.cleanupDatabase());
        verify(connection).rollback();
        verify(connection, never()).commit();
    }

    @Test
    void close_WithOpenConnection_ShouldCloseConnection() throws SQLException {
        // Given
        when(connection.isClosed()).thenReturn(false);

        // When
        databaseService.close();

        // Then
        verify(connection).close();
    }

    @Test
    void close_WithClosedConnection_ShouldNotCallClose() throws SQLException {
        // Given
        when(connection.isClosed()).thenReturn(true);

        // When
        databaseService.close();

        // Then
        verify(connection, never()).close();
    }

    @Test
    void close_CloseThrowsException_ShouldThrowStorageException() throws SQLException {
        // Given
        when(connection.isClosed()).thenReturn(false);
        doThrow(new SQLException("Close failed")).when(connection).close();

        // When & Then
        assertThrows(StorageException.class, () -> databaseService.close());
    }

    @Test
    void mergeArticles_ShouldPreferNewValuesAndAdvancedStatus() throws SQLException {
        // Given
        Article existing = Article.builder()
                .title("Old Title")
                .content("Old Content")
                .url("http://example.com")
                .status(ArticleStatus.RAW)
                .tags(List.of("old-tag"))
                .rating(3)
                .build();

        Article newArticle = Article.builder()
                .title("New Title")
                .content("New Content")
                .url("http://example.com")
                .status(ArticleStatus.ENRICHED)
                .tags(List.of("new-tag"))
                .rating(5)
                .build();

        when(articleRepository.findByUrl(newArticle.getUrl())).thenReturn(Optional.of(existing));
        when(articleRepository.findIdByUrl(newArticle.getUrl())).thenReturn(Optional.of(1L));
        when(tagRepository.getOrCreateTagId("new-tag")).thenReturn(1);

        // When
        databaseService.saveArticle(newArticle);

        // Then
        verify(articleRepository).update(argThat(article ->
                "New Title".equals(article.getTitle()) &&
                        "New Content".equals(article.getContent()) &&
                        ArticleStatus.ENRICHED.equals(article.getStatus()) &&
                        article.getRating().equals(5)
        ));
    }

    private Article createTestArticle() {
        return Article.builder()
                .title("Test Article")
                .content("Test content")
                .url("http://example.com/article")
                .author("Test Author")
                .region("US")
                .publishedAt(LocalDateTime.now())
                .sourceName("Test Source")
                .language("en")
                .status(ArticleStatus.RAW)
                .summary("Test summary")
                .rating(5)
                .tags(Arrays.asList("tech", "news"))
                .build();
    }
}