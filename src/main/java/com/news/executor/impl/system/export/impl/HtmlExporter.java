package com.news.executor.impl.system.export.impl;

import com.news.executor.impl.system.export.Exporter;
import com.news.model.Article;

import java.util.List;

public class HtmlExporter implements Exporter {
    @Override
    public String export(List<Article> articles) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>")
                .append("<html><head><title>Articles Export</title></head>")
                .append("<body><h1>Articles</h1>");

        if (articles == null || articles.isEmpty()) {
            sb.append("<p>No articles to display.</p>");
        } else {
            sb.append("<ul>");
            for (Article article : articles) {
                if (article == null) {
                    sb.append("<li><p>NULL ARTICLE</p></li>");
                    continue;
                }

                sb.append("<li>")
                        .append("<h2>").append(safeHtml(article.getTitle())).append("</h2>")
                        .append("<p><strong>URL:</strong> ").append(safeHtml(article.getUrl())).append("</p>")
                        .append("<p><strong>Summary:</strong> ").append(safeHtml(article.getSummary())).append("</p>")
                        .append("<p><strong>Content:</strong> ").append(safeHtml(article.getContent())).append("</p>")
                        .append("<p><strong>Region:</strong> ").append(safeHtml(article.getRegion())).append("</p>")
                        .append("<p><strong>Tags:</strong> ").append(formatTagsHtml(article.getTags())).append("</p>")
                        .append("<p><strong>Author:</strong> ").append(safeHtml(article.getAuthor())).append("</p>")
                        .append("<p><strong>Source:</strong> ").append(safeHtml(article.getSourceName())).append("</p>")
                        .append("<p><strong>Language:</strong> ").append(safeHtml(article.getLanguage())).append("</p>")
                        .append("<p><strong>Status:</strong> ").append(safeHtml(article.getStatus())).append("</p>")
                        .append("<p><strong>Rating:</strong> ").append(safeHtml(article.getRating())).append("</p>");

                if (article.getImageUrl() != null && !article.getImageUrl().trim().isEmpty()) {
                    sb.append("<p><img src=\"").append(safeHtml(article.getImageUrl())).append("\" alt=\"Article Image\" style=\"max-width:200px;\"></p>");
                }

                sb.append("<small><strong>Published:</strong> ").append(safeHtml(article.getPublishedAt())).append("</small>")
                        .append("</li>");
            }
            sb.append("</ul>");
        }

        sb.append("</body></html>");
        return sb.toString();
    }

    private String formatTagsHtml(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return "<em>No tags</em>";
        }
        return String.join(", ", tags);
    }

    private String safeHtml(Object value) {
        if (value == null) {
            return "<em>null</em>";
        }
        return value.toString()
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}