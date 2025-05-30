package com.news.executor.impl.system.export.impl;

import com.news.executor.impl.system.export.Exporter;
import com.news.model.Article;

import java.util.List;

public class CsvExporter implements Exporter {
    @Override
    public String export(List<Article> articles) {
        StringBuilder sb = new StringBuilder();
        sb.append("ID,Title,Content,Source,PublishedAt\n");
        for (Article article : articles) {
            sb.append(String.format("%d,\"%s\",\"%s\",\"%s\",%s\n",
                    article.getId(),
                    escape(article.getTitle()),
                    escape(article.getContent()),
                    article.getSourceName(),
                    article.getPublishedAt()));
        }
        return sb.toString();
    }

    private String escape(String text) {
        return text.replace("\"", "\"\"");
    }
}
