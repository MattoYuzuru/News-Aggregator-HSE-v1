package com.news.parser;

import com.news.model.Article;

import java.io.IOException;

public interface ArticleEnricher {
    void enrich(Article article) throws IOException;
    boolean supports(Article article);
}
