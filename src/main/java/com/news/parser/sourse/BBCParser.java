package com.news.parser.sourse;

import com.news.model.Article;
import com.news.parser.NewsSource;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BBCParser implements NewsSource {
    private static final String URL = "https://www.bbc.com/news";

    @Override
    public List<Article> fetchArticles() {
        List<Article> articles = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(URL).get();
            for (Element el : doc.select("a.gs-c-promo-heading")) {
                String title = el.text();
                String link = el.absUrl("href");
                articles.add(Article.builder().title(title).url(link).build());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return articles;
    }
}
