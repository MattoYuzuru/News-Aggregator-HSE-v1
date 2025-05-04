package com.news.parser.sourse;

import com.news.model.Article;
import com.news.parser.ArticleEnricher;
import com.news.parser.Parser;
import com.news.parser.article.NHKArticleParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.news.parser.util.TimeUtil.dateConverter;

public class NHKParser implements Parser {
    private static final String BASE_URL = "https://www3.nhk.or.jp";
    private static final String NEWS_LIST_URL = BASE_URL + "/nhkworld/en/news/list/?p=1";

    private final ArticleEnricher enricher = new NHKArticleParser();

    @Override
    public List<Article> fetchArticles() {
        List<Article> articles = new ArrayList<>();

        try {
            Document doc = Jsoup.connect(NEWS_LIST_URL).get();

            for (Element article : doc.select("article.c-mainSectionArticle")) {
                Element titleEl = article.selectFirst("h2.c-mainSectionArticle__title a");
                Element dateEl = article.selectFirst("div.c-mainSectionArticle__date");

                if (titleEl != null && dateEl != null) {
                    String title = titleEl.text();
                    String date = dateEl.attr("data-time");
                    String url = BASE_URL + titleEl.attr("href");

                    articles.add(Article.builder()
                            .title(title)
                            .url(url)
                            .sourceName("NHK")
                            .publishedAt(dateConverter(date))
                            .build());
                }
            }

            for (Element article : doc.select("div.c-articleList article.c-article")) {
                Element titleEl = article.selectFirst("h3.c-article__title a");
                Element dateEl = article.selectFirst("div.c-article__date");

                if (titleEl != null && dateEl != null) {
                    String title = titleEl.text();
                    String url = BASE_URL + titleEl.attr("href");
                    String dataTime = dateEl.attr("data-time");
                    articles.add(Article.builder()
                            .title(title)
                            .url(url)
                            .sourceName("NHK")
                            .publishedAt(dateConverter(dataTime))
                            .build());
                }
            }

            for (Element article : doc.select("div.c-articleMediaList article.c-articleMedia")) {
                Element titleEl = article.selectFirst("h3.c-articleMedia__title");
                Element linkEl = article.selectFirst("a");
                Element dateEl = article.selectFirst("div.c-article__date");

                if (titleEl != null && linkEl != null && dateEl != null) {
                    String title = titleEl.text();
                    String url = BASE_URL + linkEl.attr("href");
                    String dataTime = dateEl.attr("data-time");
                    articles.add(Article.builder()
                            .title(title)
                            .url(url)
                            .sourceName("NHK")
                            .publishedAt(dateConverter(dataTime))
                            .build());
                }
            }

        } catch (IOException e) {
            System.out.println("Failed to fetch articles from NHK: " + e.getMessage());
        }

        return articles;
    }

    @Override
    public ArticleEnricher getEnricher() {
        return enricher;
    }
}
