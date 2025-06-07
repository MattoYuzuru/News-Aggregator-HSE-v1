package com.news.executor.impl.system.export.impl;

import com.news.executor.impl.system.export.Exporter;
import com.news.model.Article;

import java.util.List;

public class CsvExporter implements Exporter {

    @Override
    public String export(List<Article> articles) {
        StringBuilder csv = new StringBuilder();

        // Add complete header with all fields
        csv.append("id,title,url,summary,content,region,tags,publishedAt,author,sourceName,imageUrl,language,status,rating\n");

        if (articles == null || articles.isEmpty()) {
            return csv.toString(); // Return header even for empty list
        }

        // Add data rows
        for (Article article : articles) {
            if (article == null) {
                // Add empty row with proper column count for null articles
                csv.append(",,,,,,,,,,,,,\n");
                continue;
            }

            csv.append(safeString(article.getId()))
                    .append(",")
                    .append(escape(safeString(article.getTitle())))
                    .append(",")
                    .append(escape(safeString(article.getUrl())))
                    .append(",")
                    .append(escape(safeString(article.getSummary())))
                    .append(",")
                    .append(escape(safeString(article.getContent())))
                    .append(",")
                    .append(escape(safeString(article.getRegion())))
                    .append(",")
                    .append(escape(formatTags(article.getTags())))
                    .append(",")
                    .append(escape(safeString(article.getPublishedAt())))
                    .append(",")
                    .append(escape(safeString(article.getAuthor())))
                    .append(",")
                    .append(escape(safeString(article.getSourceName())))
                    .append(",")
                    .append(escape(safeString(article.getImageUrl())))
                    .append(",")
                    .append(escape(safeString(article.getLanguage())))
                    .append(",")
                    .append(escape(safeString(article.getStatus())))
                    .append(",")
                    .append(safeString(article.getRating()))
                    .append("\n");
        }

        return csv.toString();
    }

    private String formatTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return "";
        }
        return String.join(";", tags); // Use semicolon to separate tags since comma is CSV delimiter
    }

    private String escape(String text) {
        if (text == null) {
            return "";
        }
        if (text.contains(",") || text.contains("\"") || text.contains("\n") || text.contains(";")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }

    private String safeString(Object value) {
        return value != null ? value.toString() : "";
    }
}