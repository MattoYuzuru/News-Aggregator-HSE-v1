package com.news;

import com.news.model.Article;
import com.news.parser.ParserService;
import com.news.parser.source.NHKParser;
import com.news.service.EnrichmentService;
import com.news.storage.StorageService;
import com.news.storage.impl.PostgresStorageService;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

public class AggregatorApp {
    public static void main(String[] args) throws Exception {
        ParserService parserService = new ParserService(List.of(new NHKParser()));
        EnrichmentService enrichmentService = new EnrichmentService();

        Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/news_db", "user", "password");
        StorageService storageService = new PostgresStorageService(connection);

        List<Article> articles = parserService.collectAllArticles();

        for (Article article : articles) {
            enrichmentService.enrich(article);
            storageService.saveArticle(article);
        }

        System.out.println("✅ Парсинг завершён. Сохранено статей: " + articles.size());
    }
}
