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

public class NHKArticleParser implements ArticleEnricher {

    @Override
    public void enrich(Article article) throws IOException {
        Document doc = Jsoup.connect(article.getUrl()).get();

        String content = doc.select("div.p-article__body").text();
        article.setContent(content);

        Elements keywordsMeta = doc.select("meta[name=keywords]");
        List<String> tags = new ArrayList<>();

        String keywords = keywordsMeta.attr("content");
        for (String keyword : keywords.split(",")) {
            tags.add(keyword.trim());
        }
        article.setTags(tags);
    }

    @Override
    public boolean supports(Article article) {
        return article.getUrl().contains("nhk.or.jp");
    }
}
