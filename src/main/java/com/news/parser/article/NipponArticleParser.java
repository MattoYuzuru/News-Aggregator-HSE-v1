package com.news.parser.article;

import com.news.model.Article;
import com.news.parser.ArticleEnricher;

import java.io.IOException;

public class NipponArticleParser implements ArticleEnricher {

    @Override
    public boolean supports(Article article) {
        return article.getUrl().contains("nippon.com");
    }

    @Override
    public void enrich(Article article) throws IOException {

    }
}
