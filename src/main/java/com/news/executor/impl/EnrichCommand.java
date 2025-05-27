package com.news.executor.impl;

import com.news.executor.Command;
import com.news.model.Article;
import com.news.model.ArticleStatus;
import com.news.model.ParsedCommand;
import com.news.parser.ArticleEnricher;
import com.news.parser.EnrichmentService;
import com.news.parser.ParserRegistry;
import com.news.parser.ParserService;
import com.news.storage.DatabaseService;

import java.util.List;

public class EnrichCommand implements Command {
    private final DatabaseService databaseService;
    private final ParserRegistry parserRegistry;

    public EnrichCommand(DatabaseService databaseService, ParserRegistry parserRegistry) {
        this.databaseService = databaseService;
        this.parserRegistry = parserRegistry;
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

        List<ArticleEnricher> enrichers = parserRegistry.getAllEnrichers();
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