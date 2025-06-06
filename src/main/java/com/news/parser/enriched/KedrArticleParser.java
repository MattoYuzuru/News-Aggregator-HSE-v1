package com.news.parser.enriched;

import com.news.model.Article;
import com.news.model.ArticleStatus;
import com.news.parser.ArticleEnricher;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public class KedrArticleParser implements ArticleEnricher {
    @Override
    public void enrich(Article article) throws IOException {
        Document document = Jsoup.connect(article.getUrl()).get();

        Elements contentEl = document.select("div.entry-content > p");
        Element titleEl = document.selectFirst("h1.entry-header__title");
        Element authorEl = document.selectFirst("span.entry-header__meta-authors > a");
        Element dateEl = document.selectFirst("span.entry-header__meta-date");
        Element imageUrlEl = document.selectFirst("figure.entry-header__image img");

        StringBuilder content = new StringBuilder();
        String title = null;
        String author = null;
        LocalDateTime publishedAt = null;
        String imageUrl = null;

        if (!contentEl.isEmpty()) {
            for (Element el : contentEl) {
                content.append(el.text());
            }
        }

        if (titleEl != null) {
            title = titleEl.text();
        }

        if (authorEl != null) {
            author = authorEl.text();
        }

        if (dateEl != null) {
            String dateString = dateEl.text();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", new Locale("ru", "RU"));

            try {
                LocalDate date = LocalDate.parse(dateString, formatter);
                publishedAt = date.atStartOfDay();
            } catch (DateTimeParseException e) {
                System.err.println("Error while parsing the date: " + dateString + " using current date");
                publishedAt = LocalDateTime.now();
            }
        }

        if (imageUrlEl != null) {
            imageUrl = imageUrlEl.attr("src");
        }


        article.setContent(content.toString());
        article.setTitle(title);
        article.setAuthor(author);
        article.setPublishedAt(publishedAt);
        article.setStatus(ArticleStatus.ENRICHED);
        article.setImageUrl("imageUrl");
    }

    @Override
    public boolean supports(Article article) {
        return article.getUrl().contains("kedr.media");
    }
}
