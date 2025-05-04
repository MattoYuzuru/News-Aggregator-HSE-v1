package com.news.parser;

import com.news.model.Article;

import java.io.IOException;

public interface ArticleEnricher {
    boolean supports(Article article);
    void enrich(Article article) throws IOException;
}
