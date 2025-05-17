package com.news.aianalysis;

import com.news.model.Article;
import com.news.model.ArticleStatus;
import com.news.storage.ArticleRepository;

import java.util.List;

public class AIAnalysisService {
    private final ArticleRepository repository;
    private final ArticleAnalyzer analyzer;
    private static final int BATCH_SIZE = 10;  // Process articles in batches
    private static final int INITIAL_DELAY_MS = 5000;  // 5 seconds
    private static final int MAX_RETRIES = 3;

    public AIAnalysisService(ArticleRepository repository, ArticleAnalyzer analyzer) {
        this.repository = repository;
        this.analyzer = analyzer;
    }

    public void enrichUnanalyzedArticles() throws InterruptedException {
        List<Article> allArticles = repository.findByStatus(ArticleStatus.ENRICHED);
        System.out.println("Found " + allArticles.size() + " articles to analyze");

        // Process in batches to avoid breaking the API
        for (int i = 0; i < allArticles.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, allArticles.size());
            List<Article> batch = allArticles.subList(i, end);
            processBatch(batch);

            // Longer delay between batches for rate limits
            if (end < allArticles.size()) {
                System.out.println("Waiting between batches...");
                Thread.sleep(INITIAL_DELAY_MS * 2);
            }
        }
    }

    private void processBatch(List<Article> articles) throws InterruptedException {
        for (Article article : articles) {
            processArticleWithRetry(article);
            // Delay between articles within a batch
            Thread.sleep(INITIAL_DELAY_MS);
        }
    }

    private void processArticleWithRetry(Article article) throws InterruptedException {
        int retries = 0;
        boolean success = false;

        while (!success && retries < MAX_RETRIES) {
            try {
                EnrichmentResult result = analyzer.analyze(article);

                // Check for valid results
                if (result != null &&
                        (result.getSummary() != null || result.getRegion() != null ||
                                (result.getTags() != null && !result.getTags().isEmpty()))) {

                    System.out.println("Successfully analyzed article: " + article.getTitle());
                    article.setSummary(result.getSummary());
                    article.setRegion(result.getRegion());
                    article.setTags(result.getTags());
                    article.setStatus(ArticleStatus.ANALYZED);
                    System.out.println("Successfully added new content to the DB");
                    repository.update(article);
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
            repository.update(article);
        }
    }
}