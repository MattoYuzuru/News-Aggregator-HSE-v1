package com.news;

import com.news.model.Article;

import com.news.parser.ParserService;
import com.news.parser.source.BBCParser;
import com.news.parser.source.NHKParser;
import com.news.parser.source.NipponParser;

import java.util.List;

public class AggregatorApp {
    public static void main(String[] args) {
        ParserService parserService = new ParserService(List.of(
                new NHKParser(),
                new BBCParser(),
                new NipponParser()
        ));

        List<Article> articles = parserService.collectAllArticles();
        articles.forEach(System.out::println);

    }
}
