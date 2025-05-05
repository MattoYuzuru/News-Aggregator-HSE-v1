package com.news.parser.source;

import com.news.model.Article;
import com.news.parser.ArticleEnricher;
import com.news.parser.Parser;
import com.news.parser.article.NipponArticleParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class NipponParser implements Parser {
    private static final String SITEMAP_URL = "https://www.nippon.com/ru/articles.xml";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private final ArticleEnricher enricher = new NipponArticleParser();

    @Override
    public List<Article> fetchArticles() {
        List<Article> articles = new ArrayList<>();

        try {
            Document xmlDoc = Jsoup.connect(SITEMAP_URL).parser(org.jsoup.parser.Parser.xmlParser()).get();
            Elements urlElements = xmlDoc.select("url");

            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(1);

            for (Element urlElement : urlElements) {

                Element locEl = urlElement.selectFirst("loc");
                Element lastmodEl = urlElement.selectFirst("lastmod");

                if (locEl == null || lastmodEl == null) {
                    continue;
                }

                String url = locEl.text();
                String lastmodStr = lastmodEl.text();

                LocalDateTime publishedAt;
                try {
                    publishedAt = LocalDateTime.parse(lastmodStr, FORMATTER);
                } catch (Exception e) {
                    continue;
                }

                if (publishedAt.isBefore(sevenDaysAgo)) {
                    break; // Stop processing older articles
                }

                articles.add(Article.builder()
                        .url(url)
                        .publishedAt(publishedAt)
                        .sourceName("nippon.com")
                        .language("ru")
                        .build());
            }
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
