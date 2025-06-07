package com.news.ai.operation;

import com.news.ai.client.AIClient;
import com.news.model.Article;

public class SummarizationOperation implements AIOperation<String> {
    private final AIClient client;

    public SummarizationOperation(AIClient client) {
        this.client = client;
    }

    @Override
    public String execute(Article article) throws Exception {
        String content = article.getContent();
        if (content == null || content.isBlank()) {
            return null;
        }
        return client.summarize(content);
    }

    @Override
    public String getOperationName() {
        return "summarization";
    }
}