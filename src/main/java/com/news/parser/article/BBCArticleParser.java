package com.news.parser.article;

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

import static com.news.parser.util.TimeUtil.ISOConverter;

public class BBCArticleParser implements ArticleEnricher {

    @Override
    public void enrich(Article article) throws IOException {
        Document document = Jsoup.connect(article.getUrl()).get();

        Elements contentEl = document.select("div[data-component=\"text-block\"], div[data-component=\"subheadline-block\"]");
        Elements tagsEl = document.select("div[data-component=tags]");
        Element authorEl = document.select("div[data-testid=byline-new-contributors]").selectFirst("span");
        Element timeEl = document.selectFirst("time");

        if (authorEl != null && timeEl != null) {
            String author = authorEl.text();
            String time = timeEl.attr("datetime");

            List<String> blocks = new ArrayList<>();
            for (Element div : contentEl) {
                blocks.add(div.text());
            }
            String content = String.join(" ", blocks);

            List<String> tags = new ArrayList<>();
            for (Element div : tagsEl) {
                tags.add(div.text());
            }

            article.setContent(content);
            article.setTags(tags);
            article.setAuthor(author);
            article.setPublishedAt(ISOConverter(time));
            article.setStatus(ArticleStatus.ENRICHED);
        }
    }

    @Override
    public boolean supports(Article article) {
        return article.getUrl().contains("bbc.com");
    }
}
