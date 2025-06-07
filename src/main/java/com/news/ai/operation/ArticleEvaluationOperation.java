package com.news.ai.operation;

import com.news.ai.client.AIClient;
import com.news.model.Article;

public class ArticleEvaluationOperation implements AIOperation<Integer> {
    private final AIClient client;

    public ArticleEvaluationOperation(AIClient client) {
        this.client = client;
    }

    @Override
    public Integer execute(Article article) throws Exception {
        String content = article.getContent();
        if (content == null || content.isBlank()) {
            return null;
        }
        return client.evaluateArticle(content);
    }

    @Override
    public String getOperationName() {
        return "evaluation";
    }
}