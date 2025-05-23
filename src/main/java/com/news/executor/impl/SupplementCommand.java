package com.news.executor.impl;

import com.news.aianalysis.AIAnalysisService;
import com.news.aianalysis.ArticleAnalyzer;
import com.news.aianalysis.OllamaQwenArticleAnalyzer;
import com.news.executor.Command;
import com.news.model.Article;
import com.news.model.ArticleStatus;
import com.news.model.ParsedCommand;
import com.news.storage.DatabaseService;

import java.util.List;

public class SupplementCommand implements Command {
    private final DatabaseService databaseService;

    public SupplementCommand(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @Override
    public void execute(ParsedCommand parsedCommand) {
        List<Article> articles;

        System.out.println("Fetching all ENRICHED articles");
        articles = databaseService.getArticleRepository().findByStatus(ArticleStatus.ENRICHED);

        if (articles.isEmpty()) {
            System.out.println("No articles found for AI enrichment. Run command 'enrich' first to continue.");
            return;
        }

        System.out.println("Found " + articles.size() + " articles for AI enrichment...");

        ArticleAnalyzer articleAnalyzer = new OllamaQwenArticleAnalyzer();
        AIAnalysisService analysisService = new AIAnalysisService(databaseService, articleAnalyzer);

        int savedCount = analysisService.analyzeArticles(articles);

        System.out.println("Successfully supplemented and updated " + savedCount + " articles");
    }
}
