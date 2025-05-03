package com.news;

import com.news.model.Article;
import com.news.parser.sourse.BBCParser;
import com.news.parser.sourse.NHKParser;
import com.news.parser.ParserService;

import java.util.List;

public class AggregatorApp {
    public static void main(String[] args) {
        ParserService parserService = new ParserService(List.of(new BBCParser()));
        List<Article> articles = parserService.collectAllArticles();
        articles.forEach(System.out::println);
    }
}
