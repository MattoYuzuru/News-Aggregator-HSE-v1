package com.news.parser.sourse;

import com.news.model.Article;
import com.news.parser.NewsSource;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.news.parser.util.TimeUtil.dateFromString;

public class BBCParser implements NewsSource {
    private static final String URL = "https://www.bbc.com/news";

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
                Element dateEl = card.selectFirst("span[data-testid=card-metadata-lastupdated]");
                Element regionEl = card.selectFirst("span[data-testid=card-metadata-tag]");

                String title = titleEl != null ? titleEl.text() : "Untitled";
                String date = dateEl != null ? dateEl.text() : "Unknown";
                String region = regionEl != null ? regionEl.text() : "Unknown";

                articles.add(Article.builder()
                        .title(title)
                        .url(url)
                        .content("content")
                        .region(region)
                        .publishedAt(dateFromString(date))
                        .build());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return articles;
    }
}
