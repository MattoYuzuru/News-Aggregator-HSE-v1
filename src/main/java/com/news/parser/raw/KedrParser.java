package com.news.parser.raw;

import com.news.model.Article;
import com.news.model.ArticleStatus;
import com.news.parser.ArticleEnricher;
import com.news.parser.Parser;
import com.news.parser.enriched.KedrArticleParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class KedrParser implements Parser {
    private final static String BASE_URL = "https://kedr.media/category";
    private final static List<String> SECTIONS = List.of(
//            "/research",
//            "/stories",
            "/news"
    );

    private final static ArticleEnricher enricher = new KedrArticleParser();

    @Override
    public List<Article> fetchArticles() {
        List<Article> articles = new ArrayList<>();

        for (String subUrl : SECTIONS) {
            try {
                Document document = Jsoup.connect(BASE_URL + subUrl).get();

                for (Element urlEl : document.select("a.frame-news__content-title")) {
                    String url = urlEl.attr("href");

                    Article article = Article.builder()
                            .url(url)
                            .language("ru")
                            .status(ArticleStatus.RAW)
                            .sourceName("kedr")
                            .build();

                    articles.add(article);
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return articles;
    }

    @Override
    public ArticleEnricher getEnricher() {
        return enricher;
    }
}
