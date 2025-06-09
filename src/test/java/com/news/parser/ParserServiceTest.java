package com.news.parser;

import com.news.model.Article;
import com.news.model.ArticleStatus;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ParserServiceTest {

    // Test implementation of Parser
    static class TestParser implements Parser {
        private final List<Article> articlesToReturn;
        private final AtomicInteger fetchCallCount = new AtomicInteger(0);

        TestParser(List<Article> articlesToReturn) {
            this.articlesToReturn = articlesToReturn;
        }

        @Override
        public List<Article> fetchArticles() {
            fetchCallCount.incrementAndGet();
            return articlesToReturn;
        }

        @Override
        public ArticleEnricher getEnricher() {
            return null; // Not used in these tests
        }

        public int getFetchCallCount() {
            return fetchCallCount.get();
        }
    }

    private Article createTestArticle(String title, String source) {
        return Article.builder()
                .title(title)
                .url("http://example.com/" + title)
                .sourceName(source)
                .status(ArticleStatus.RAW)
                .build();
    }

    @Test
    void emptyParsersReturnEmptyList() {
        // Arrange
        ParserService service = new ParserService(Collections.emptyList(), null);

        // Act
        List<Article> result = service.collectAllArticles();

        // Assert
        assertTrue(result.isEmpty(), "Should return empty list when no parsers provided");
    }

    @Test
    void collectAllArticlesWithoutLimit() {
        // Arrange
        List<Article> parser1Articles = List.of(
                createTestArticle("Article1", "Source1"),
                createTestArticle("Article2", "Source1")
        );
        List<Article> parser2Articles = List.of(
                createTestArticle("Article3", "Source2"),
                createTestArticle("Article4", "Source2"),
                createTestArticle("Article5", "Source2")
        );

        TestParser parser1 = new TestParser(parser1Articles);
        TestParser parser2 = new TestParser(parser2Articles);
        ParserService service = new ParserService(List.of(parser1, parser2), null);

        // Act
        List<Article> result = service.collectAllArticles();

        // Assert
        assertEquals(5, result.size(), "Should return all articles from all parsers");
        assertEquals(1, parser1.getFetchCallCount(), "Parser1 should be called once");
        assertEquals(1, parser2.getFetchCallCount(), "Parser2 should be called once");

        // Check that all articles are present
        assertTrue(result.stream().anyMatch(a -> a.getTitle().equals("Article1")));
        assertTrue(result.stream().anyMatch(a -> a.getTitle().equals("Article5")));
    }

    @Test
    void collectAllArticlesWithZeroLimitReturnsEmpty() {
        // Arrange
        List<Article> articles = List.of(createTestArticle("Article1", "Source1"));
        TestParser parser = new TestParser(articles);
        ParserService service = new ParserService(List.of(parser), 0);

        // Act
        List<Article> result = service.collectAllArticles();

        // Assert
        assertTrue(result.isEmpty(), "Should return empty list when limit is 0");
    }

    @Test
    void collectAllArticlesWithNegativeLimitReturnsEmpty() {
        // Arrange
        List<Article> articles = List.of(createTestArticle("Article1", "Source1"));
        TestParser parser = new TestParser(articles);
        ParserService service = new ParserService(List.of(parser), -5);

        // Act
        List<Article> result = service.collectAllArticles();

        // Assert
        assertTrue(result.isEmpty(), "Should return empty list when limit is negative");
    }

    @Test
    void collectAllArticlesWithLimitDistributesEvenly() {
        // Arrange
        List<Article> parser1Articles = List.of(
                createTestArticle("Article1", "Source1"),
                createTestArticle("Article2", "Source1"),
                createTestArticle("Article3", "Source1")
        );
        List<Article> parser2Articles = List.of(
                createTestArticle("Article4", "Source2"),
                createTestArticle("Article5", "Source2"),
                createTestArticle("Article6", "Source2")
        );

        TestParser parser1 = new TestParser(parser1Articles);
        TestParser parser2 = new TestParser(parser2Articles);
        ParserService service = new ParserService(List.of(parser1, parser2), 4);

        // Act
        List<Article> result = service.collectAllArticles();

        // Assert
        assertEquals(4, result.size(), "Should return exactly 4 articles");
        // With limit 4 and 2 parsers: 4/2 = 2 each, remainder 0
        // So each parser should contribute 2 articles
        long source1Count = result.stream().filter(a -> a.getSourceName().equals("Source1")).count();
        long source2Count = result.stream().filter(a -> a.getSourceName().equals("Source2")).count();

        assertEquals(2, source1Count, "Source1 should contribute 2 articles");
        assertEquals(2, source2Count, "Source2 should contribute 2 articles");
    }

    @Test
    void collectAllArticlesWithLimitDistributesRemainder() {
        // Arrange
        List<Article> parser1Articles = List.of(
                createTestArticle("Article1", "Source1"),
                createTestArticle("Article2", "Source1"),
                createTestArticle("Article3", "Source1")
        );
        List<Article> parser2Articles = List.of(
                createTestArticle("Article4", "Source2"),
                createTestArticle("Article5", "Source2"),
                createTestArticle("Article6", "Source2")
        );
        List<Article> parser3Articles = List.of(
                createTestArticle("Article7", "Source3"),
                createTestArticle("Article8", "Source3")
        );

        TestParser parser1 = new TestParser(parser1Articles);
        TestParser parser2 = new TestParser(parser2Articles);
        TestParser parser3 = new TestParser(parser3Articles);
        ParserService service = new ParserService(List.of(parser1, parser2, parser3), 5);

        // Act
        List<Article> result = service.collectAllArticles();

        // Assert
        assertEquals(5, result.size(), "Should return exactly 5 articles");
        // With limit 5 and 3 parsers: 5/3 = 1 each, remainder 2
        // So first 2 parsers get 2 articles each, third gets 1
        long source1Count = result.stream().filter(a -> a.getSourceName().equals("Source1")).count();
        long source2Count = result.stream().filter(a -> a.getSourceName().equals("Source2")).count();
        long source3Count = result.stream().filter(a -> a.getSourceName().equals("Source3")).count();

        assertEquals(2, source1Count, "Source1 should contribute 2 articles (base + remainder)");
        assertEquals(2, source2Count, "Source2 should contribute 2 articles (base + remainder)");
        assertEquals(1, source3Count, "Source3 should contribute 1 article (base only)");
    }

    @Test
    void collectAllArticlesWithLimitHigherThanAvailable() {
        // Arrange
        List<Article> parser1Articles = List.of(createTestArticle("Article1", "Source1"));
        List<Article> parser2Articles = List.of(createTestArticle("Article2", "Source2"));

        TestParser parser1 = new TestParser(parser1Articles);
        TestParser parser2 = new TestParser(parser2Articles);
        ParserService service = new ParserService(List.of(parser1, parser2), 10);

        // Act
        List<Article> result = service.collectAllArticles();

        // Assert
        assertEquals(2, result.size(), "Should return all available articles when limit is higher");
    }

    @Test
    void collectAllArticlesParallelWithoutLimit() {
        // Arrange
        List<Article> parser1Articles = List.of(
                createTestArticle("Article1", "Source1"),
                createTestArticle("Article2", "Source1")
        );
        List<Article> parser2Articles = List.of(
                createTestArticle("Article3", "Source2")
        );

        TestParser parser1 = new TestParser(parser1Articles);
        TestParser parser2 = new TestParser(parser2Articles);
        ParserService service = new ParserService(List.of(parser1, parser2), null);

        // Act
        List<Article> result = service.collectAllArticlesParallel();

        // Assert
        assertEquals(3, result.size(), "Should return all articles from all parsers");
        assertEquals(1, parser1.getFetchCallCount(), "Parser1 should be called once");
        assertEquals(1, parser2.getFetchCallCount(), "Parser2 should be called once");
    }

    @Test
    void collectAllArticlesParallelWithLimit() {
        // Arrange
        List<Article> parser1Articles = List.of(
                createTestArticle("Article1", "Source1"),
                createTestArticle("Article2", "Source1"),
                createTestArticle("Article3", "Source1")
        );
        List<Article> parser2Articles = List.of(
                createTestArticle("Article4", "Source2"),
                createTestArticle("Article5", "Source2"),
                createTestArticle("Article6", "Source2")
        );

        TestParser parser1 = new TestParser(parser1Articles);
        TestParser parser2 = new TestParser(parser2Articles);
        ParserService service = new ParserService(List.of(parser1, parser2), 4);

        // Act
        List<Article> result = service.collectAllArticlesParallel();

        // Assert
        assertEquals(4, result.size(), "Should return limited number of articles");
        assertEquals(1, parser1.getFetchCallCount(), "Parser1 should be called once");
        assertEquals(1, parser2.getFetchCallCount(), "Parser2 should be called once");
    }

    @Test
    void collectAllArticlesParallelWithZeroLimit() {
        // Arrange
        List<Article> articles = List.of(createTestArticle("Article1", "Source1"));
        TestParser parser = new TestParser(articles);
        ParserService service = new ParserService(List.of(parser), 0);

        // Act
        List<Article> result = service.collectAllArticlesParallel();

        // Assert
        assertTrue(result.isEmpty(), "Should return empty list when limit is 0");
    }

    @Test
    void singleParserSingleArticle() {
        // Arrange
        List<Article> articles = List.of(createTestArticle("SingleArticle", "SingleSource"));
        TestParser parser = new TestParser(articles);
        ParserService service = new ParserService(List.of(parser), null);

        // Act
        List<Article> result = service.collectAllArticles();

        // Assert
        assertEquals(1, result.size(), "Should return single article");
        assertEquals("SingleArticle", result.get(0).getTitle());
        assertEquals("SingleSource", result.get(0).getSourceName());
    }
}