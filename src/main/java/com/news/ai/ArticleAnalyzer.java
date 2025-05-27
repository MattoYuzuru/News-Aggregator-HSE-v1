package com.news.ai;

import com.news.model.Article;

public interface ArticleAnalyzer {
    EnrichmentResult analyze(Article article) throws InterruptedException;
}