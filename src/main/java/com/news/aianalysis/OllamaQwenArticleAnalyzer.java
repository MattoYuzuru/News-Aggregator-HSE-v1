package com.news.aianalysis;

import com.news.model.Article;

import java.util.ArrayList;
import java.util.List;

public class HuggingFaceArticleAnalyzer implements ArticleAnalyzer {

    private final OllamaClient client = new OllamaClient();

    @Override
    public EnrichmentResult analyze(Article article) throws InterruptedException {
        String content = article.getContent();
        if (content == null || content.isBlank() || content.equals("content")) {
            return EnrichmentResult.empty();
        }

        String summary = null;
        String region = null;
        List<String> tags = new ArrayList<>();

        try {
            summary = client.summarize(content);
            if (summary != null && !summary.isEmpty()) {
                System.out.println("Generated summary: " + summary.substring(0, Math.min(50, summary.length())) + "...");
            } else {
                System.out.println("Failed to generate summary");
            }
        } catch (Exception e) {
            System.err.println("Error generating summary: " + e.getMessage());
        }

        try {
            region = client.classifyRegion(content);
            if (region != null && !region.isEmpty()) {
                System.out.println("Classified region: " + region);
            } else {
                System.out.println("Failed to classify region");
            }
        } catch (Exception e) {
            System.err.println("Error classifying region: " + e.getMessage());
        }

        try {
            List<String> generatedTags = client.generateTags(content);
            if (generatedTags != null && !generatedTags.isEmpty()) {
                tags = generatedTags;
                System.out.println("Generated tags: " + tags);
            } else {
                System.out.println("Failed to generate tags");
            }
        } catch (Exception e) {
            System.err.println("Error generating tags: " + e.getMessage());
        }

        return EnrichmentResult.builder()
                .summary(summary)
                .region(region)
                .tags(tags)
                .build();
    }
}