package com.news.executor.impl.system.export;

import com.news.model.Article;
import com.news.model.ExportFormat;
import com.news.storage.ArticleRepository;

import java.util.List;
import java.util.NoSuchElementException;

public class ExportService {
    private final ArticleRepository repository;

    public ExportService(ArticleRepository repository) {
        this.repository = repository;
    }

    public String exportAll(ExportFormat format) {
        List<Article> allArticles = repository.findAll();
        return export(allArticles, format);
    }

    public String exportById(Long id, ExportFormat format) {
        Article article = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Article not found with id: " + id));
        return export(List.of(article), format);
    }

    private String export(List<Article> articles, ExportFormat format) {
        Exporter exporter = ExporterFactory.getExporter(format);
        return exporter.export(articles);
    }
}
