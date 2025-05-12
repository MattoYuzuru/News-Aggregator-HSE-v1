package com.news.parser;

import com.news.model.Article;

import java.util.ArrayList;
import java.util.List;

public class ParserService {
    private final List<Parser> parsers;

    public ParserService(List<Parser> parsers) {
        this.parsers = parsers;
    }

    public List<Article> collectAllArticles() {
        List<Article> all = new ArrayList<>();

        for (Parser parser : parsers) {
            List<Article> articles = parser.fetchArticles();
            all.addAll(articles);
        }

        return all;
    }
}
