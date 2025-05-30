package com.news.executor.impl.system.export.impl;

import com.news.executor.impl.system.export.Exporter;
import com.news.model.Article;

import java.util.List;

public class HtmlExporter implements Exporter {
    @Override
    public String export(List<Article> articles) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body><h1>Articles</h1><ul>");
        for (Article article : articles) {
            sb.append("<li>")
                    .append("<h2>").append(article.getTitle()).append("</h2>")
                    .append("<p>").append(article.getContent()).append("</p>")
                    .append("<small>").append(article.getSourceName()).append(" - ")
                    .append(article.getPublishedAt()).append("</small>")
                    .append("</li>");
        }
        sb.append("</ul></body></html>");
        return sb.toString();
    }
}
