package com.news.parser.article;

import com.news.model.Article;
import com.news.parser.ArticleEnricher;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NipponArticleParser implements ArticleEnricher {
    @Override
    public void enrich(Article article) throws IOException {
        Document document = Jsoup.connect(article.getUrl())
                .userAgent("\"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/123.0.0.0 Safari/537.36\"")
                .header("Accept-Language", "en-US,en;q=0.9")
                .header("Accept-Encoding", "gzip, deflate")
                .get();

        Element titleEl = document.selectFirst("h1");
        Elements contentEl = document.select("div[class='editArea']");
        // section.c-detailkeyward -> tags without separation
        Elements tagsEl = document.select("section.c-detailkeyward a");
        Element authorEl = document.selectFirst("meta[name=\"cXenseParse:author\"]");

        if (titleEl != null) {
            String title = titleEl.text();
            article.setTitle(title);
        }

        if (authorEl != null) {
            String author = authorEl.attr("content");
            article.setAuthor(author);
        }

        List<String> tags = new ArrayList<>();
        for (Element el : tagsEl) {
            tags.add(el.text());
        }
        article.setTags(tags);

        String content = contentEl.text();
        article.setContent(content);
    }

    @Override
    public boolean supports(Article article) {
        return article.getUrl().contains("nippon.com");
    }

    public static void main(String[] args) throws IOException {
        Document doc = Jsoup.connect("https://www.nippon.com/ru/japan-data/h02382/").get();
        System.out.println(doc.html());
    }
}
