package com.news.aianalysis;

import com.news.model.Article;
import com.news.model.ArticleStatus;
import com.news.storage.ArticleRepository;
import com.news.storage.impl.JdbcArticleTagLinker;
import com.news.storage.impl.JdbcTagRepository;

import java.sql.Connection;
import java.util.List;

public class AIAnalysisService {
    private final ArticleRepository repository;
    private final ArticleAnalyzer analyzer;
    private final JdbcTagRepository tagRepository;
    private final JdbcArticleTagLinker tagLinker;
    private static final int INITIAL_DELAY_MS = 5000;  // 5 seconds
    private static final int MAX_RETRIES = 3;

    public AIAnalysisService(ArticleRepository repository, ArticleAnalyzer analyzer, Connection connection) {
        this.repository = repository;
        this.analyzer = analyzer;
        this.tagRepository = new JdbcTagRepository(connection);
        this.tagLinker = new JdbcArticleTagLinker(connection);
    }

    public void enrichUnanalyzedArticles() throws InterruptedException {
        List<Article> allArticles = repository.findByStatus(ArticleStatus.ENRICHED);
        System.out.println("Found " + allArticles.size() + " articles to analyze");
        for (Article article : allArticles) {
            processArticleWithRetry(article);
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
                        (result.getSummary() != null || result.getRegion() != null || result.getTags() != null)) {

                    System.out.println("Successfully analyzed article: " + article.getTitle());
                    article.setSummary(result.getSummary());

                    // Update region only if it's not null and not empty (If appeared before)
                    if (result.getRegion() != null && !result.getRegion().isBlank()) {
                        article.setRegion(result.getRegion());
                    }

                    article.setTags(result.getTags());
                    article.setStatus(ArticleStatus.ANALYZED);

                    repository.update(article);

                    if (article.getTags() != null && !article.getTags().isEmpty()) {
                        int articleId = repository.findIdByUrl(article.getUrl())
                                .orElseThrow(() -> new IllegalStateException("Article not found after update"));

                        for (String tag : article.getTags()) {
                            int tagId = tagRepository.getOrCreateTagId(tag);
                            tagLinker.linkArticleTags(articleId, tagId);
                        }
                    }
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