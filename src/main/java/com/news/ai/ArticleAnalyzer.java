package com.news.aianalysis;

import com.news.model.Article;

public interface ArticleAnalyzer {
    EnrichmentResult analyze(Article article) throws InterruptedException;
}