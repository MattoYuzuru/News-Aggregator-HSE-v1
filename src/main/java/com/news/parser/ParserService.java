package com.news.parser;

import com.news.model.Article;

import java.util.ArrayList;
import java.util.List;

public class ParserService {
    private final List<NewsSource> sources;

    public ParserService(List<NewsSource> sources) {
        this.sources = sources;
    }

    public List<Article> collectAllArticles() {
        List<Article> all = new ArrayList<>();

        for (NewsSource source : sources) {
            all.addAll(source.fetchArticles());
        }
        return all;
    }
}
