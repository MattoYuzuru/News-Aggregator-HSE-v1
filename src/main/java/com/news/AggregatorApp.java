package com.news;

import com.news.model.Article;

import com.news.parser.ArticleEnricher;
import com.news.parser.sourse.BBCParser;
import com.news.parser.sourse.NHKParser;

import com.news.parser.article.NHKArticleParser;

import com.news.parser.ParserService;

import java.io.IOException;
import java.util.List;

public class AggregatorApp {
    public static void main(String[] args) {
        ParserService parserService = new ParserService(List.of(new NHKParser(), new BBCParser()));
        List<Article> articles = parserService.collectAllArticles();

        List<ArticleEnricher> enrichers = List.of(new NHKArticleParser());

        // Filling article's 'content' field
        for (Article article : articles) {
            for (ArticleEnricher enricher : enrichers) {
                if (enricher.supports(article)) {
                    try {
                        enricher.enrich(article);
                    } catch (IOException e) {
                        System.out.println("Error while enriching article: " + article.getUrl());
                    }
                }
            }
        }

        articles.forEach(System.out::println);
    }
}
