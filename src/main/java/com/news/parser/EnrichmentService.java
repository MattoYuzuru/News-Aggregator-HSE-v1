package com.news.parser;

import com.news.model.Article;

import java.io.IOException;
import java.util.List;

public class EnrichmentService {
    private final List<ArticleEnricher> enrichers;

    public EnrichmentService(List<ArticleEnricher> enrichers) {
        this.enrichers = enrichers;
    }

    public void enrichAll(List<Article> articles) {
        for (Article article : articles) {
            for (ArticleEnricher enricher : enrichers) {
                if (enricher.supports(article)) {
                    try {
                        enricher.enrich(article);
                    } catch (IOException e) {
                        System.out.println("Failed to enrich article: " + article.getUrl());
                        System.out.println("Error: " + e.getMessage());
                    }
                    break;
                }
            }
        }
    }
}
