package com.news.parser.source;

import com.news.model.Article;
import com.news.model.ArticleStatus;
import com.news.parser.ArticleEnricher;
import com.news.parser.Parser;
import com.news.parser.article.BBCArticleParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class BBCParser implements Parser {
    private static final String URL = "https://www.bbc.com/news";

    private final ArticleEnricher enricher = new BBCArticleParser();

    @Override
    public List<Article> fetchArticles() {
        List<Article> articles = new ArrayList<>();
        Set<String> uniqueArticles = new HashSet<>();

        try {
            Document doc = Jsoup.connect(URL).get();

            for (Element card : doc.select("div[data-testid=dundee-card]")) {
                Element urlEl = card.selectFirst("a[data-testid=internal-link]");
                if (urlEl == null) continue;

                String url = "https://www.bbc.com" + urlEl.attr("href");

                if (uniqueArticles.contains(url)) continue; // Skipping duplicated article
                uniqueArticles.add(url);

                Element titleEl = card.selectFirst("h2[data-testid=card-headline]");
                Element regionEl = card.selectFirst("span[data-testid=card-metadata-tag]");

                String title = titleEl != null ? titleEl.text() : "Untitled";
                String region = regionEl != null ? regionEl.text() : "Unknown";

                articles.add(Article.builder()
                        .title(title)
                        .url(url)
                        .content("content")
                        .region(region)
                        .sourceName("BBC")
                        .language("eng")
                        .status(ArticleStatus.RAW)
                        .build());
            }
        } catch (IOException e) {
            System.out.println("Failed to fetch articles from BBC: " + e.getMessage());
        }

        return articles;
    }

    @Override
    public ArticleEnricher getEnricher() {
        return enricher;
    }
}
