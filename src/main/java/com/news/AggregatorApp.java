package com.news;

import com.news.aianalysis.AIAnalysisService;
import com.news.aianalysis.ArticleAnalyzer;
import com.news.aianalysis.OllamaQwenArticleAnalyzer;
import com.news.model.Article;
import com.news.parser.ArticleEnricher;
import com.news.parser.Parser;
import com.news.parser.ParserService;
import com.news.parser.EnrichmentService;
import com.news.parser.source.BBCParser;
import com.news.parser.source.NHKParser;
import com.news.parser.source.NipponParser;
import com.news.storage.*;
import com.news.storage.impl.JdbcArticleRepository;
import com.news.storage.impl.JdbcTagRepository;
import com.news.storage.impl.JdbcArticleTagLinker;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class AggregatorApp {

    private final static List<Parser> parsers = List.of(
            new NHKParser()
//            new NipponParser(),
//            new BBCParser()
    );

    private static List<Article> getArticles(EnrichmentService enrichmentService) {
        ParserService parserService = new ParserService(parsers, null);
        List<Article> articles = parserService.collectAllArticles();
        enrichmentService.enrichAll(articles);
        return articles;
    }

    private static List<ArticleEnricher> getEnrichers() {
        return AggregatorApp.parsers.stream()
                .map(Parser::getEnricher)
                .toList();
    }

    public static void main(String[] args) {
        try (Connection connection = DatabaseConfig.getConnection()) {
            // 1. Init enrichers
            List<ArticleEnricher> enrichers = getEnrichers();

            EnrichmentService enrichmentService = new EnrichmentService(enrichers);

            List<Article> articles = getArticles(enrichmentService);

            // 2. Repo Init
            ArticleRepository articleRepo = new JdbcArticleRepository(connection);
            TagRepository tagRepo = new JdbcTagRepository(connection);
            ArticleTagLinker tagLinker = new JdbcArticleTagLinker(connection);

            // 3. Save Articles and Tags
            for (Article article : articles) {
                articleRepo.save(article);
                if (article.getTags() != null && !article.getTags().isEmpty()) {
                    int articleId = articleRepo.findIdByUrl(article.getUrl())
                            .orElseThrow(() -> new IllegalStateException("Article not found after insert"));

                    for (String tag : article.getTags()) {
                        int tagId = tagRepo.getOrCreateTagId(tag);
                        tagLinker.linkArticleTags(articleId, tagId);
                    }
                }
            }

            // 4. Enrich Articles with AI
            ArticleAnalyzer articleAnalyzer = new OllamaQwenArticleAnalyzer();
            AIAnalysisService analysisService = new AIAnalysisService(articleRepo, articleAnalyzer, connection);

            analysisService.enrichUnanalyzedArticles();

            articleRepo.findAll().forEach(System.out::println);

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
