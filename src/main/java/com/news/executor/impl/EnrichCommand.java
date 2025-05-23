package com.news.executor.impl;

import com.news.executor.Command;
import com.news.model.Article;
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
        List<ArticleEnricher> enrichers = AVAILABLE_PARSERS.values().stream()
                .map(Supplier::get)
                .map(Parser::getEnricher)
                .toList();
        EnrichmentService enrichmentService = new EnrichmentService(enrichers);
        List<Article> articles = ;
        enrichmentService.enrichAll(articles);

        int savedCount = 0;
        for (Article article: articles) {
            try {
                databaseService.saveArticle(article);
                savedCount++;
            } catch (Exception e) {
                System.err.println("Failed to sace article: " + article.getUrl());
                e.printStackTrace();
            }
        }
        System.out.println("Successfully parsed and saved " + savedCount + " articles");

    }
}
