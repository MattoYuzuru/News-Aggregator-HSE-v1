package com.news.storage;

import com.news.model.Article;

import java.util.List;

public interface StorageService {
    void saveArticle(Article article);
    void saveArticles(List<Article> articles);
    boolean articleExists(String url);
}
