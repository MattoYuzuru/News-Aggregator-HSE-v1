package com.news.ai;

import com.news.model.Article;
import com.news.model.ArticleStatus;
import com.news.storage.DatabaseService;

import java.util.List;

public class AIAnalysisService {
    private final DatabaseService databaseService;
    private final ArticleAnalyzer analyzer;
    private static final int INITIAL_DELAY_MS = 2500;  // 2.5 seconds
    private static final int MAX_RETRIES = 3;

    public AIAnalysisService(DatabaseService databaseService, ArticleAnalyzer analyzer) {
        this.databaseService = databaseService;
        this.analyzer = analyzer;
    }

    public int analyzeArticles(List<Article> articles) {
        int successCount = 0;

        System.out.println("Starting AI analysis of " + articles.size() + " articles...");

        for (Article article : articles) {
            try {
                if (processArticleWithRetry(article)) {
                    successCount++;
                }
            } catch (InterruptedException e) {
                System.err.println("Analysis interrupted for article: " + article.getTitle());
                Thread.currentThread().interrupt(); // Preserve interrupt status
                break;
            }
        }

        return successCount;
    }

    private boolean processArticleWithRetry(Article article) throws InterruptedException {
        int retries = 0;
        boolean success = false;

        while (!success && retries < MAX_RETRIES) {
            try {
                EnrichmentResult result = analyzer.analyze(article);

                // Check for valid results
                if (result != null &&
                        (result.getSummary() != null || result.getRegion() != null || result.getTags() != null)) {

                    System.out.println("Successfully analyzed article: " + article.getTitle());

                    // Update article with AI analysis results
                    if (result.getSummary() != null) {
                        article.setSummary(result.getSummary());
                    }

                    // Update region only if it's not null and not empty
                    if (result.getRegion() != null && !result.getRegion().isBlank()) {
                        article.setRegion(result.getRegion());
                    }

                    if (result.getTags() != null) {
                        article.setTags(result.getTags());
                    }

                    // Update status
                    article.setStatus(ArticleStatus.ANALYZED);

                    // Save to database
                    databaseService.saveArticle(article);

                    success = true;
                } else {
                    System.out.println("Incomplete result for article: " + article.getTitle());
                    retries++;
                    // Exponential backoff for retries
                    Thread.sleep((long) (INITIAL_DELAY_MS * Math.pow(2, retries)));
                }
            } catch (Exception e) {
                System.err.println("Error analyzing article " + article.getTitle() + ": " + e.getMessage());
                retries++;
                // Exponential backoff for retries
                Thread.sleep((long) (INITIAL_DELAY_MS * Math.pow(2, retries)));
            }
        }

        if (!success) {
            System.err.println("Failed to analyze article after " + MAX_RETRIES + " attempts: " + article.getTitle());
            article.setStatus(ArticleStatus.ERROR);
            try {
                databaseService.saveArticle(article);
            } catch (Exception e) {
                System.err.println("Failed to update article status to ERROR: " + e.getMessage());
            }
        }

        return success;
    }
}