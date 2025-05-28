package com.news.parser;

import com.news.model.Article;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class ParserServiceTest {

    static class DummyParser implements Parser {
        private final List<Article> articles;

        DummyParser(List<Article> articles) {
            this.articles = articles;
        }

        @Override
        public List<Article> fetchArticles() {
            return articles;
        }

        @Override
        public ArticleEnricher getEnricher() {
            return null;
        }
    }

    @Test
    void returnsEmptyListIfNoParsers() {
        ParserService service = new ParserService(Collections.emptyList(), 10);
        assertTrue(service.collectAllArticles().isEmpty());
    }

    @Test
    void aggregatesArticlesFromAllParsers() {
        Article a = Article.builder().title("A").build();
        Article b = Article.builder().title("B").build();
        Parser p1 = new DummyParser(List.of(a));
        Parser p2 = new DummyParser(List.of(b));
        ParserService service = new ParserService(List.of(p1, p2), null);
        List<Article> res = service.collectAllArticles();
        assertEquals(2, res.size());
        assertTrue(res.contains(a) && res.contains(b));
    }

    @Test
    void respectsLimitAcrossParsers() {
        Article a = Article.builder().title("A").build();
        Article b = Article.builder().title("B").build();
        Article c = Article.builder().title("C").build();
        Parser p1 = new DummyParser(List.of(a, b));
        Parser p2 = new DummyParser(List.of(c));
        ParserService service = new ParserService(List.of(p1, p2), 2);
        List<Article> res = service.collectAllArticles();
        assertEquals(2, res.size());
    }

    @Test
    void limitZeroReturnsEmpty() {
        Article a = Article.builder().title("A").build();
        Parser p1 = new DummyParser(List.of(a));
        ParserService service = new ParserService(List.of(p1), 0);
        assertTrue(service.collectAllArticles().isEmpty());
    }
}