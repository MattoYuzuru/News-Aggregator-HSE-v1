package com.news;

import com.news.model.Article;
import com.news.parser.Parser;
import com.news.parser.ParserService;
import com.news.parser.source.BBCParser;
import com.news.parser.source.NHKParser;
import com.news.parser.source.NipponParser;
import com.news.parser.EnrichmentService;
import com.news.storage.ArticleRepository;
import com.news.storage.DatabaseConfig;
import com.news.storage.impl.JdbcArticleRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AggregatorApp {
    public static void main(String[] args) throws SQLException {
        List<Parser> parsers = List.of(
                new NHKParser(),
                new BBCParser(),
                new NipponParser()
        );

        ParserService parserService = new ParserService(parsers);
        EnrichmentService enrichmentService = new EnrichmentService();

        List<Article> allArticles = new ArrayList<>();

        for (Parser parser : parsers) {
            List<Article> articles = parser.fetchArticles();
            enrichmentService.enrichAll(articles, parser.getEnricher());
            allArticles.addAll(articles);
        }
        Connection connection = DatabaseConfig.getConnection();
        ArticleRepository repository = new JdbcArticleRepository(connection);

        repository.saveAll(allArticles);
        allArticles.forEach(System.out::println);
    }
}
