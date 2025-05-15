package com.news.aianalysis;

import com.news.model.ArticleStatus;
import com.news.storage.ArticleRepository;
import com.news.model.Article;

import java.util.List;

public class AIAnalysisService {
    private final ArticleRepository repository;
    private final ArticleAnalyzer analyzer;

    public AIAnalysisService(ArticleRepository repository, ArticleAnalyzer analyzer) {
        this.repository = repository;
        this.analyzer = analyzer;
    }

    public void enrichUnanalyzedArticles() throws InterruptedException {
        List<Article> articles = repository.findByStatus(ArticleStatus.ENRICHED);

        for (Article article : articles) {
            EnrichmentResult result = analyzer.analyze(article);
            System.out.println(result.getRegion() + " " + result.getSummary() + " " + result.getTags());
            article.setSummary(result.getSummary());
            article.setRegion(result.getRegion());
            article.setTags(result.getTags());
            article.setStatus(ArticleStatus.ANALYZED);
            repository.update(article);
        }
    }
}
