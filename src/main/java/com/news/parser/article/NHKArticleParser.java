package com.news.parser.article;

import com.news.model.Article;
import com.news.parser.ArticleEnricher;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.List;

public class NHKArticleParser implements ArticleEnricher {

    @Override
    public void enrich(Article article) throws IOException {
        Document doc = Jsoup.connect(article.getUrl()).get();
        String content = doc.select("div.p-article__body").text();
        List<String> tags = doc.select("ul.p-newsDetail__tags li").eachText();
        System.out.println(tags);
        article.setContent(content);
        article.setTags(tags);
    }

    @Override
    public boolean supports(Article article) {
        return article.getUrl().contains("nhk.or.jp");
    }
}
