package com.news.parser;

import com.news.model.Article;

import java.util.List;


public interface NewsSource {
    List<Article> fetchArticles();
}
