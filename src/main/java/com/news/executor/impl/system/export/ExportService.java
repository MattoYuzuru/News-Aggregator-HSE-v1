package com.news.executor.impl.system.export;

import com.news.model.Article;
import com.news.model.ExportFormat;
import com.news.storage.ArticleRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class ExportService {
    private final ArticleRepository repository;
    private static final int BATCH_SIZE = 100;

    public ExportService(ArticleRepository repository) {
        this.repository = repository;
    }

    public String exportAll(ExportFormat format) {
        List<Article> allArticles = repository.findAll();

        if (allArticles.size() > BATCH_SIZE) {
            return exportWithVirtualThreads(allArticles, format);
        } else {
            // When <= BATCH_SIZE, use usual processing
            return export(allArticles, format);
        }
    }

    public String exportById(Long id, ExportFormat format) {
        Article article = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Article not found with id: " + id));
        return export(List.of(article), format);
    }

    private String exportWithVirtualThreads(List<Article> articles, ExportFormat format) {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            // Create batches for processing
            List<List<Article>> batches = createBatches(articles);

            // Submit each batch as a virtual thread task
            List<Future<String>> futures = batches.stream()
                    .map(batch -> executor.submit(() -> {
                        Exporter exporter = ExporterFactory.getExporter(format);
                        return exporter.export(batch);
                    }))
                    .toList();

            StringBuilder combinedResult = new StringBuilder();
            for (Future<String> future : futures) {
                try {
                    String batchResult = future.get();
                    combinedResult.append(batchResult);
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException("Error processing batch", e);
                }
            }

            return combineResults(combinedResult.toString(), format);

        } catch (Exception e) {
            // Fallback
            System.err.println("Virtual thread processing failed: " + e.getMessage());
            return export(articles, format);
        }
    }

    private String export(List<Article> articles, ExportFormat format) {
        Exporter exporter = ExporterFactory.getExporter(format);
        return exporter.export(articles);
    }

    private List<List<Article>> createBatches(List<Article> articles) {
        return new ArrayList<>(articles.stream()
                .collect(Collectors.groupingBy(article -> articles.indexOf(article) / ExportService.BATCH_SIZE))
                .values());
    }

    private String combineResults(String results, ExportFormat format) {
        return switch (format) {
            case JSON -> combineJsonResults(results);
            case CSV -> combineCsvResults(results);
            case HTML -> combineHtmlResults(results);
        };
    }

    private String combineJsonResults(String results) {
        // Remove individual array brackets and create one combined array
        String cleanResults = results.replaceAll("\\[\\s*\\]", "") // Remove empty arrays
                .replaceAll("\\[", "")          // Remove opening brackets
                .replaceAll("\\]", "")          // Remove closing brackets
                .replaceAll(",\\s*,", ",")      // Clean up double commas
                .trim();

        if (cleanResults.isEmpty()) {
            return "[]";
        }

        return "[" + cleanResults + "]";
    }

    private String combineCsvResults(String results) {
        String[] lines = results.split("\n");
        StringBuilder combined = new StringBuilder();

        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            combined.append(line).append("\n");
        }

        return combined.toString();
    }

    private String combineHtmlResults(String results) {
        // Combine HTML content, removing duplicate HTML wrapper tags if they exist
        String cleanResults = results.replaceAll("</?html>", "")
                .replaceAll("</?body>", "")
                .trim();

        return "<html><body>" + cleanResults + "</body></html>";
    }
}