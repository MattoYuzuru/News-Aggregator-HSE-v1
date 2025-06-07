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
            List<List<Article>> batches = createBatches(articles);

            if (format == ExportFormat.CSV) {
                return exportCsvWithBatches(batches, executor);
            }

            List<Future<String>> futures = batches.stream()
                    .map(batch -> executor.submit(() -> {
                        Exporter exporter = ExporterFactory.getExporter(format);
                        return exporter.export(batch);
                    }))
                    .toList();

            List<String> results = new ArrayList<>();
            for (Future<String> future : futures) {
                try {
                    results.add(future.get());
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException("Error processing batch", e);
                }
            }

            return combineResults(results, format);

        } catch (Exception e) {
            System.err.println("Virtual thread processing failed: " + e.getMessage());
            return export(articles, format);
        }
    }

    private String exportCsvWithBatches(List<List<Article>> batches, ExecutorService executor) {
        StringBuilder combinedCsv = new StringBuilder();

        combinedCsv.append("id,title,url,summary,content,region,tags,publishedAt,author,sourceName,imageUrl,language,status,rating\n");

        List<Future<String>> futures = batches.stream()
                .map(batch -> executor.submit(() -> {
                    Exporter exporter = ExporterFactory.getExporter(ExportFormat.CSV);
                    String result = exporter.export(batch);
                    String[] lines = result.split("\n");
                    if (lines.length > 1) {
                        StringBuilder batchData = new StringBuilder();
                        for (int i = 1; i < lines.length; i++) {
                            if (!lines[i].trim().isEmpty()) {
                                batchData.append(lines[i]).append("\n");
                            }
                        }
                        return batchData.toString();
                    }
                    return "";
                }))
                .toList();

        for (Future<String> future : futures) {
            try {
                String batchResult = future.get();
                combinedCsv.append(batchResult);
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("Error processing batch", e);
            }
        }

        return combinedCsv.toString();
    }

    private String export(List<Article> articles, ExportFormat format) {
        Exporter exporter = ExporterFactory.getExporter(format);
        return exporter.export(articles);
    }

    private List<List<Article>> createBatches(List<Article> articles) {
        List<List<Article>> batches = new ArrayList<>();
        for (int i = 0; i < articles.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, articles.size());
            batches.add(articles.subList(i, end));
        }
        return batches;
    }

    private String combineResults(List<String> results, ExportFormat format) {
        return switch (format) {
            case JSON -> combineJsonResults(results);
            case HTML -> combineHtmlResults(results);
            case CSV -> combineCsvResults(results);
        };
    }

    private String combineJsonResults(List<String> results) {
        StringBuilder combined = new StringBuilder();
        combined.append("[");

        boolean first = true;
        for (String result : results) {
            if (result == null || result.trim().equals("[]")) continue;

            // Remove outer brackets and extract content
            String content = result.trim();
            if (content.startsWith("[") && content.endsWith("]")) {
                content = content.substring(1, content.length() - 1).trim();
            }

            if (!content.isEmpty()) {
                if (!first) {
                    combined.append(",");
                }
                combined.append(content);
                first = false;
            }
        }

        combined.append("]");
        return combined.toString();
    }

    private String combineCsvResults(List<String> results) {
        StringBuilder combined = new StringBuilder();
        boolean headerAdded = false;

        for (String result : results) {
            if (result == null || result.trim().isEmpty()) continue;

            String[] lines = result.split("\n");
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.isEmpty()) continue;

                if (i == 0 && !headerAdded) {
                    combined.append(line).append("\n");
                    headerAdded = true;
                } else if (i > 0) {
                    combined.append(line).append("\n");
                }
            }
        }

        return combined.toString();
    }

    private String combineHtmlResults(List<String> results) {
        StringBuilder combined = new StringBuilder();
        combined.append("<!DOCTYPE html><html><head><title>Articles Export</title></head><body><h1>Articles</h1><ul>");

        for (String result : results) {
            if (result == null) continue;

            String content = result;
            if (content.contains("<ul>") && content.contains("</ul>")) {
                int start = content.indexOf("<ul>") + 4;
                int end = content.lastIndexOf("</ul>");
                if (start < end) {
                    content = content.substring(start, end);
                    combined.append(content);
                }
            }
        }

        combined.append("</ul></body></html>");
        return combined.toString();
    }
}