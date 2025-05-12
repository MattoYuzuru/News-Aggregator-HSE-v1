package com.news.parser;

import com.news.model.Article;

import java.io.IOException;
import java.util.List;

public class EnrichmentService {
    public void enrichAll(List<Article> articles, ArticleEnricher enricher) {
        for (Article article : articles) {
            try {
                enricher.enrich(article);
            } catch (IOException e) {
                System.out.println("Failed to enrich article: " + article.getUrl());
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
}
