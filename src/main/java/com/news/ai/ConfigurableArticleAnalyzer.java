package com.news.ai;

import com.news.ai.client.AIClient;
import com.news.ai.config.AIConfiguration;
import com.news.ai.factory.AIServiceFactory;
import com.news.ai.operation.AIOperation;
import com.news.model.Article;

import java.util.List;
import java.util.Map;

public class ConfigurableArticleAnalyzer implements ArticleAnalyzer {
    private final Map<String, AIOperation<?>> operations;

    public ConfigurableArticleAnalyzer(AIConfiguration config) {
        AIClient client = AIServiceFactory.createClient(config.modelName());
        this.operations = AIServiceFactory.createOperations(client, config);
    }

    @Override
    public EnrichmentResult analyze(Article article) throws InterruptedException {

        // No point in analyzing empty article
        String content = article.getContent();
        if (content == null || content.isBlank()) {
            return EnrichmentResult.empty();
        }
        EnrichmentResult.EnrichmentResultBuilder builder = EnrichmentResult.builder();

        // Execute summarization if enabled
        if (operations.containsKey("summarization")) {
            try {
                String summary = (String) operations.get("summarization").execute(article);
                builder.summary(summary);
                if (summary == null || summary.isEmpty()) {
                    System.out.println("Failed to generate summary");
                }
            } catch (Exception e) {
                System.err.println("Error generating summary: " + e.getMessage());
            }
        }

        // Execute region classification if enabled
        if (operations.containsKey("region_classification")) {
            try {
                String region = (String) operations.get("region_classification").execute(article);
                builder.region(region);
                if (region == null || region.isBlank()) {
                    System.out.println("Failed to classify region");
                }
            } catch (Exception e) {
                System.err.println("Error classifying region: " + e.getMessage());
            }
        }

        // Execute tag generation if enabled
        if (operations.containsKey("tag_generation")) {
            try {
                @SuppressWarnings("unchecked") // W
                List<String> tags = (List<String>) operations.get("tag_generation").execute(article);
                builder.tags(tags);
                if (tags == null || tags.isEmpty()) {
                    System.out.println("Failed to generate tags");
                }
            } catch (Exception e) {
                System.err.println("Error generating tags: " + e.getMessage());
            }
        }

        // Execute evaluation if enabled
        if (operations.containsKey("evaluation")) {
            try {
                Integer rating = (Integer) operations.get("evaluation").execute(article);
                builder.rating(rating);
                if (rating == null) {
                    System.out.println("Failed to evaluate article");
                }
            } catch (Exception e) {
                System.err.println("Error evaluating article: " + e.getMessage());
            }
        }

        return builder.build();
    }
}