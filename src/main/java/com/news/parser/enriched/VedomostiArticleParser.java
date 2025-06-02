package com.news.parser.enriched;

import com.news.model.Article;
import com.news.parser.ArticleEnricher;

import java.io.IOException;

public class VedomostiArticleParser implements ArticleEnricher {
    @Override
    public void enrich(Article article) throws IOException {

    }

    @Override
    public boolean supports(Article article) {
        return false;
    }
}
