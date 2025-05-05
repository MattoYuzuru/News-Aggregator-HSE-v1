package com.news.parser.sourse;

import org.jsoup.Jsoup;
import com.news.model.Article;
import com.news.parser.Parser;
import org.jsoup.nodes.Document;
import com.news.parser.ArticleEnricher;
import com.news.parser.article.NipponArticleParser;

import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

public class NipponParser implements Parser {
    private static final String BASE_URL = "https://www.nippon.com";
    private static final String LATEST_NEWS_URL = BASE_URL + "/ru/latest/";
//    private static final String LATEST_NEWS_URL = "https://www.nippon.com/ru/people/e00203/";

    private final ArticleEnricher enricher = new NipponArticleParser();

    @Override
    public List<Article> fetchArticles() {
        List<Article> articles = new ArrayList<>();

        try {
            Document document = Jsoup.connect(LATEST_NEWS_URL).get();


        } catch (IOException e) {
            System.out.println("Failed to fetch articles from nippon.com: " + e.getMessage());
        }

        return articles;
    }

    @Override
    public ArticleEnricher getEnricher() {
        return enricher;
    }
}
