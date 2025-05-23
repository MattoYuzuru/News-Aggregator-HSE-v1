package com.news.executor.impl;

import com.news.executor.Command;
import com.news.model.Article;
import com.news.model.ArticleStatus;
import com.news.model.ParsedCommand;
import com.news.parser.ArticleEnricher;
import com.news.parser.EnrichmentService;
import com.news.parser.Parser;
import com.news.storage.DatabaseService;

import java.util.List;
import java.util.function.Supplier;

import static com.news.parser.ParserService.AVAILABLE_PARSERS;

public class EnrichCommand implements Command {
    private final DatabaseService databaseService;

    public EnrichCommand(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @Override
    public void execute(ParsedCommand parsedCommand) {
        List<Article> articles;

        System.out.println("Fetching all RAW articles");
        articles = databaseService.getArticleRepository().findByStatus(ArticleStatus.RAW);

        if (articles.isEmpty()) {
            System.out.println("No articles found for enrichment. Run 'parse' command first to get articles.");
            return;
        }

        System.out.println("Found " + articles.size() + " articles for enrichment...");

        List<ArticleEnricher> enrichers = AVAILABLE_PARSERS.values().stream()
                .map(Supplier::get)
                .map(Parser::getEnricher)
                .toList();

        EnrichmentService enrichmentService = new EnrichmentService(enrichers);

        enrichmentService.enrichAll(articles);

        int savedCount = 0;
        for (Article article : articles) {
            try {
                databaseService.saveArticle(article);
                savedCount++;
            } catch (Exception e) {
                System.err.println("Failed to save article: " + article.getUrl());
                e.printStackTrace();
            }
        }

        System.out.println("Successfully enriched and updated " + savedCount + " articles");
    }
}