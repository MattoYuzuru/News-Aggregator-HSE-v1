package com.news.parser;

import com.news.model.Article;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.news.parser.TimeUtil.dateConverter;

public class NHKParser implements NewsSource {
    private static final String BASE_URL = "https://www3.nhk.or.jp";
    private static final String NEWS_LIST_URL = BASE_URL + "/nhkworld/en/news/list/?p=1";

    @Override
    public List<Article> fetchArticles() {
        List<Article> articles = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(NEWS_LIST_URL).get();
            for (Element article : doc.select("div.c-articleList article.c-article")) {
                String title = Objects.requireNonNull(article.selectFirst("h3.c-article__title a")).text();
                String relativeLink = Objects.requireNonNull(article.selectFirst("h3.c-article__title a")).attr("href");
                String url = BASE_URL + relativeLink;
                String dataTime = Objects.requireNonNull(article.selectFirst("div.c-article__date")).attr("data-time");
                articles.add(new Article(title, url, "content", dateConverter(dataTime)));
            }
        } catch (IOException e) {
            System.out.println("Failed to connect to NHK: " + e.getMessage());
        }
        try {
            Document doc = Jsoup.connect(NEWS_LIST_URL).get();
            for (Element article : doc.select("div.c-articleMediaList article.c-articleMedia")) {
                String title = Objects.requireNonNull(article.selectFirst("h3.c-articleMedia__title")).text();
                String relativeLink = Objects.requireNonNull(article.selectFirst("article.c-articleMedia a")).attr("href");
                String url = BASE_URL + relativeLink;
                String dataTime = Objects.requireNonNull(article.selectFirst("div.c-article__date")).attr("data-time");
                articles.add(new Article(title, url, "content", dateConverter(dataTime)));
            }
        } catch (IOException e) {
            System.out.println("Failed to connect to NHK: " + e.getMessage());
        }
        return articles;
    }

    // main title : p-article2__title
    // main content : p-article2__content / p-article__body
    // main tags ul : p-newsDetail__tags c-tags2
    // lists c-articleMediaList / c-articleMedia__title / c-article__date
}


