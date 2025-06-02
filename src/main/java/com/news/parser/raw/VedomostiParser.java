package com.news.parser.raw;

import com.news.model.Article;
import com.news.parser.ArticleEnricher;
import com.news.parser.Parser;
import com.news.parser.enriched.VedomostiArticleParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VedomostiParser implements Parser {
    private static final String BASE_URL = "https://www.vedomosti.ru";
    private static final String[] PAGES = {
            "/tourism",
            "/technologies",
            "/think"
    };
    private static final ArticleEnricher enricher = new VedomostiArticleParser();

    @Override
    public List<Article> fetchArticles() {
        List<Article> articles = new ArrayList<>();
        Set<String> uniqueUrls = new HashSet<>();

        try {
            for (String page : PAGES) {
                System.out.println("Parsing page: " + BASE_URL + page);
                Document document = Jsoup.connect(BASE_URL + page)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                        .timeout(10000)
                        .get();

                Elements articleCards = document.select(".grid__body .articles-cards-list__card");

                for (Element card : articleCards) {
                    try {
                        Article article = parseArticleFromCard(card);
                        if (article != null && uniqueUrls.add(article.getUrl())) {
                            articles.add(article);
                            System.out.println("Found article: " + article.getUrl());
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing article card: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error fetching articles", e);
        }

        System.out.println("Total unique articles found: " + articles.size());
        return articles;
    }

    private Article parseArticleFromCard(Element card) {
        if (card.hasClass("card-banner")) {
            return null;
        }

        String url = null;
        String imageUrl = null;

        Element linkElement = card.select("a[href]").first();
        if (linkElement != null) {
            String href = linkElement.attr("href");
            if (href.startsWith("/")) {
                url = BASE_URL + href;
            } else if (href.startsWith("http")) {
                url = href;
            }
        }

        Element imageElement = card.select("img").first();
        if (imageElement != null) {
            String src = imageElement.attr("src");
            if (!src.isEmpty()) {
                imageUrl = src;
            }
        }

        if (url != null && url.contains("/articles/")) {
            Article article = new Article();
            article.setUrl(url);
            article.setImageUrl(imageUrl);
            return article;
        }

        return null;
    }

    @Override
    public ArticleEnricher getEnricher() {
        return enricher;
    }

    public static void main(String[] args) throws IOException {
//        VedomostiParser parser = new VedomostiParser();
//        List<Article> articles = parser.fetchArticles();
//
//        System.out.println("\n=== PARSED ARTICLES ===");
//        for (Article article : articles) {
//            System.out.println("URL: " + article.getUrl());
//            System.out.println("Image URL: " + article.getImageUrl());
//            System.out.println("---");
//        }
        Document document = Jsoup.connect(BASE_URL + "/tourism")
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(10000)
                .get();
        String articleCards = document.html();
        System.out.println(articleCards);
    }
}