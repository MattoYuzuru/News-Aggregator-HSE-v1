package com.news.parser.article;

import com.news.model.Article;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class NHKArticleParser {
    public void enrichArticle(Article article) throws IOException {
        Document doc = Jsoup.connect(article.getUrl()).get();
        String content = doc.select("div.p-article__body").text();
        article.setContent(content);
    }
}

