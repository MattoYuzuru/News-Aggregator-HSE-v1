package com.news.ai.operation;

import com.news.ai.client.AIClient;
import com.news.model.Article;

import java.util.List;

public class TagGenerationOperation implements AIOperation<List<String>> {
    private final AIClient client;

    public TagGenerationOperation(AIClient client) {
        this.client = client;
    }

    @Override
    public List<String> execute(Article article) throws Exception {
        String content = article.getContent();
        if (content == null || content.isBlank()) {
            return List.of();
        }
        return client.generateTags(content);
    }

    @Override
    public String getOperationName() {
        return "tag_generation";
    }
}