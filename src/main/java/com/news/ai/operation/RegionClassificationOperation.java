package com.news.ai.operation;

import com.news.ai.client.AIClient;
import com.news.model.Article;

public class RegionClassificationOperation implements AIOperation<String> {
    private final AIClient client;

    public RegionClassificationOperation(AIClient client) {
        this.client = client;
    }

    @Override
    public String execute(Article article) throws Exception {
        String content = article.getContent();
        if (content == null || content.isBlank()) {
            return null;
        }

        // Only classify if region is not already set
        String existingRegion = article.getRegion();
        if (existingRegion != null && !existingRegion.isEmpty()) {
            return existingRegion;
        }

        return client.classifyRegion(content);
    }

    @Override
    public String getOperationName() {
        return "region_classification";
    }
}