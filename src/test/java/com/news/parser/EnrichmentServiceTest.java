package com.news.parser;

import com.news.model.Article;
import lombok.Getter;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnrichmentServiceTest {

    // Test implementation of ArticleEnricher
    static class TestEnricher implements ArticleEnricher {
        private final boolean doesSupport;
        private final boolean throwsException;
        @Getter
        private int enrichCalls = 0;

        TestEnricher(boolean doesSupport, boolean throwsException) {
            this.doesSupport = doesSupport;
            this.throwsException = throwsException;
        }

        @Override
        public boolean supports(Article article) {
            return doesSupport;
        }

        @Override
        public void enrich(Article article) throws IOException {
            enrichCalls++;
            if (throwsException) {
                throw new IOException("Test exception");
            }
            article.setTitle(article.getTitle() + "_enriched_" + this.hashCode());
        }
    }

    private Article createTestArticle(String title) {
        Article article = new Article();
        article.setTitle(title);
        article.setUrl("http://example.com/" + title);
        return article;
    }

    @Test
    void noEnrichersDoesNothing() {
        // Arrange
        EnrichmentService service = new EnrichmentService(Collections.emptyList());
        List<Article> articles = List.of(createTestArticle("Article1"));
        String originalTitle = articles.getFirst().getTitle();

        // Act
        service.enrichAll(articles);

        // Assert
        assertEquals(originalTitle, articles.getFirst().getTitle(), "Article should remain unchanged when no enrichers are present");
    }

    @Test
    void noSupportingEnrichersDoesNothing() {
        // Arrange
        TestEnricher nonSupportingEnricher = new TestEnricher(false, false);
        EnrichmentService service = new EnrichmentService(List.of(nonSupportingEnricher));
        List<Article> articles = List.of(createTestArticle("Article1"));
        String originalTitle = articles.getFirst().getTitle();

        // Act
        service.enrichAll(articles);

        // Assert
        assertEquals(originalTitle, articles.getFirst().getTitle(), "Article should remain unchanged when no enrichers support it");
        assertEquals(0, nonSupportingEnricher.getEnrichCalls(), "Enrich should not be called for non-supporting enrichers");
    }

    @Test
    void supportingEnricherEnrichesArticle() {
        // Arrange
        TestEnricher supportingEnricher = new TestEnricher(true, false);
        EnrichmentService service = new EnrichmentService(List.of(supportingEnricher));
        Article article = createTestArticle("Article1");
        List<Article> articles = List.of(article);

        // Act
        service.enrichAll(articles);

        // Assert
        assertTrue(article.getTitle().contains("_enriched_"), "Article should be enriched");
        assertEquals(1, supportingEnricher.getEnrichCalls(), "Enrich should be called once");
    }

    @Test
    void exceptionDuringEnrichmentIsCaught() {
        // Arrange
        TestEnricher exceptionThrowingEnricher = new TestEnricher(true, true);
        EnrichmentService service = new EnrichmentService(List.of(exceptionThrowingEnricher));
        Article article = createTestArticle("Article1");
        List<Article> articles = List.of(article);
        String originalTitle = article.getTitle();

        // Act
        service.enrichAll(articles);

        // Assert
        assertEquals(originalTitle, article.getTitle(), "Article should remain unchanged when enricher throws exception");
        assertEquals(1, exceptionThrowingEnricher.getEnrichCalls(), "Enrich should be called once");
    }

    @Test
    void onlyFirstSupportingEnricherIsUsed() {
        // Arrange
        TestEnricher firstEnricher = new TestEnricher(true, false);
        TestEnricher secondEnricher = new TestEnricher(true, false);
        EnrichmentService service = new EnrichmentService(List.of(firstEnricher, secondEnricher));
        Article article = createTestArticle("Article1");
        List<Article> articles = List.of(article);

        // Act
        service.enrichAll(articles);

        // Assert
        assertEquals(1, firstEnricher.getEnrichCalls(), "First enricher should be called once");
        assertEquals(0, secondEnricher.getEnrichCalls(), "Second enricher should not be called");
        assertTrue(article.getTitle().contains("_enriched_"), "Article should be enriched");
    }

    @Test
    void doesNotSkipToNextEnricherAfterException() {
        // Arrange
        TestEnricher failingEnricher = new TestEnricher(true, true);
        TestEnricher backupEnricher = new TestEnricher(true, false);
        EnrichmentService service = new EnrichmentService(List.of(failingEnricher, backupEnricher));
        Article article = createTestArticle("Article1");
        List<Article> articles = List.of(article);

        // Act
        service.enrichAll(articles);

        // Assert
        assertEquals(1, failingEnricher.getEnrichCalls(), "Failing enricher should be called once");
        assertEquals(0, backupEnricher.getEnrichCalls(), "Backup enricher should NOT be called - system stops after first supporting enricher");
        assertEquals("Article1", article.getTitle(), "Article should remain unchanged after exception");
    }

    @Test
    void multipleArticlesAreProcessed() {
        // Arrange
        TestEnricher supportingEnricher = new TestEnricher(true, false);
        EnrichmentService service = new EnrichmentService(List.of(supportingEnricher));
        Article article1 = createTestArticle("Article1");
        Article article2 = createTestArticle("Article2");
        List<Article> articles = List.of(article1, article2);

        // Act
        service.enrichAll(articles);

        // Assert
        assertTrue(article1.getTitle().contains("_enriched_"), "First article should be enriched");
        assertTrue(article2.getTitle().contains("_enriched_"), "Second article should be enriched");
        assertEquals(2, supportingEnricher.getEnrichCalls(), "Enrich should be called twice, once for each article");
    }

    @Test
    void differentEnrichersForDifferentArticles() {
        // Arrange
        TestEnricher firstEnricher = new TestEnricher(true, false) {
            @Override
            public boolean supports(Article article) {
                return article.getUrl().contains("Article1");
            }
        };
        TestEnricher secondEnricher = new TestEnricher(true, false) {
            @Override
            public boolean supports(Article article) {
                return article.getUrl().contains("Article2");
            }
        };

        EnrichmentService service = new EnrichmentService(List.of(firstEnricher, secondEnricher));
        Article article1 = createTestArticle("Article1");
        Article article2 = createTestArticle("Article2");
        List<Article> articles = List.of(article1, article2);

        // Act
        service.enrichAll(articles);

        // Assert
        assertEquals(1, firstEnricher.getEnrichCalls(), "First enricher should be called once for Article1");
        assertEquals(1, secondEnricher.getEnrichCalls(), "Second enricher should be called once for Article2");
        assertTrue(article1.getTitle().contains("_enriched_"), "Article1 should be enriched");
        assertTrue(article2.getTitle().contains("_enriched_"), "Article2 should be enriched");
    }
}