package com.news.parser.enriched;

import com.news.model.Article;
import com.news.model.ArticleStatus;
import com.news.parser.ArticleEnricher;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class KommersantArticleParser implements ArticleEnricher {

    @Override
    public void enrich(Article article) throws IOException {
        Document document = Jsoup.connect(article.getUrl())
                .userAgent("\"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/123.0.0.0 Safari/537.36\"")
                .header("Accept-Language", "en-US,en;q=0.9")
                .header("Accept-Encoding", "gzip, deflate")
                .get();

        Element titleEl = document.selectFirst("article[data-article-title]");
        Element authorEl = document.selectFirst("p.document_authors");
        Element contentEl = document.selectFirst("div.article_text_wrapper");
        Element tagsEl = document.selectFirst("article[data-analytics-rubrics]");

        String title = titleEl != null ? titleEl.attr("data-article-title") : null;
        String author = authorEl != null ? authorEl.attr("document_authors") : null;


        String content = null;
        if (contentEl != null) {
            StringBuilder sb = new StringBuilder();
            Elements paragraphs = contentEl.select("p:not(figcaption p)");
            for (Element paragraph : paragraphs) {
                sb.append(paragraph.text());
            }
            content = sb.toString();
        }

        List<String> tags = new ArrayList<>();
        if (tagsEl != null) {
            tags.add(tagsEl.attr("data-analytics-rubrics").trim());
        }

        article.setContent(content);
        article.setAuthor(author);
        article.setTags(tags);
        article.setTitle(title);
        article.setStatus(ArticleStatus.ENRICHED);
    }

    @Override
    public boolean supports(Article article) {
        return article.getUrl().contains("kommersant.ru");
    }
}
