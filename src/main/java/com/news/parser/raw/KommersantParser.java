package com.news.parser.raw;

import com.news.model.Article;
import com.news.model.ArticleStatus;
import com.news.parser.ArticleEnricher;
import com.news.parser.Parser;
import com.news.parser.enriched.KommersantArticleParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.news.ConfigLoader.FORMATTER;

public class KommersantParser implements Parser {
    private final ArticleEnricher enricher = new KommersantArticleParser();

    private final static String BASE_URL = "https://www.kommersant.ru/sitemaps/";

    @Override
    public List<Article> fetchArticles() {
        List<Article> articles = new ArrayList<>();

        for (int i = 2; i < 6; i++) {
            // there is a strange naming, but 2, 3, 4, 5 mean politics, economy etc
            String rubricUrl = String.format(BASE_URL + "sitemap_rubric_%d.xml", i);

            try {
                Document document = Jsoup.connect(rubricUrl).parser(org.jsoup.parser.Parser.xmlParser()).get();

                Elements urlElements = document.select("url");

                LocalDateTime oneDaysAgo = LocalDateTime.now().minusDays(1);

                for (Element urlElement : urlElements) {
                    Element locEl = urlElement.selectFirst("loc");
                    Element dateEl = urlElement.selectFirst("news\\:publication_date");
                    Element imageUrlEl = urlElement.selectFirst("image\\:loc");

                    if (locEl == null || dateEl == null) {
                        continue;
                    }

                    String url = locEl.text();
                    String date = dateEl.text();
                    String imageUrl = imageUrlEl != null ? imageUrlEl.text() : "No image provided";

                    LocalDateTime publishedAt;
                    try {
                        publishedAt = LocalDateTime.parse(date, FORMATTER);
                    } catch (Exception e) {
                        continue;
                    }

                    if (publishedAt.isBefore(oneDaysAgo)) {
                        break;
                    }

                    articles.add(Article.builder()
                            .url(url)
                            .publishedAt(publishedAt)
                            .sourceName("kommersant")
                            .imageUrl(imageUrl)
                            .language("ru")
                            .status(ArticleStatus.RAW)
                            .build()
                    );
                }

            } catch (IOException e) {
                System.out.println("Failed to fetch articles from Kommersant: " + e.getMessage());
            }
        }

        return articles;
    }

    @Override
    public ArticleEnricher getEnricher() {
        return enricher;
    }
}
