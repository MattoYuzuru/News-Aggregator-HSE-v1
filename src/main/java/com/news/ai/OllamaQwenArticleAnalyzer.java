package com.news.ai;

import com.news.model.Article;
import java.util.ArrayList;
import java.util.List;

public class OllamaQwenArticleAnalyzer implements ArticleAnalyzer {

    private final OllamaClient client = new OllamaClient();

    @Override
    public EnrichmentResult analyze(Article article) {
        String content = article.getContent();
        if (content == null || content.isBlank() || content.equals("content")) {
            return EnrichmentResult.empty();
        }

        String summary = null;
        String region = null;
        List<String> tags = new ArrayList<>();

        try {
            summary = OllamaClient.summarize(content);
            if (summary.isEmpty()) {
                System.out.println("Failed to generate summary");
            }
        } catch (Exception e) {
            System.err.println("Error generating summary: " + e.getMessage());
        }

        try {
            String articleRegion = article.getRegion();
            if (articleRegion == null || articleRegion.isEmpty()) {
                region = client.classifyRegion(content);
                if (region == null || region.isBlank()) {
                    System.out.println("Failed to classify region");
                }
            }
        } catch (Exception e) {
            System.err.println("Error classifying region: " + e.getMessage());
        }

        try {
            List<String> generatedTags = client.generateTags(content);
            if (generatedTags != null && !generatedTags.isEmpty()) {
                tags.addAll(generatedTags);
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