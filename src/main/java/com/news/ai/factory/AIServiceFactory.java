package com.news.ai.factory;

import com.news.ai.client.AIClient;
import com.news.ai.client.OllamaClient;
import com.news.ai.config.AIConfiguration;
import com.news.ai.operation.*;

import java.util.HashMap;
import java.util.Map;

public class AIServiceFactory {

    public static AIClient createClient(String modelName) {
        return new OllamaClient(modelName);
    }

    public static Map<String, AIOperation<?>> createOperations(AIClient client, AIConfiguration config) {
        Map<String, AIOperation<?>> operations = new HashMap<>();

        if (config.isOperationEnabled("summarization")) {
            operations.put("summarization", new SummarizationOperation(client));
        }

        if (config.isOperationEnabled("region_classification")) {
            operations.put("region_classification", new RegionClassificationOperation(client));
        }

        if (config.isOperationEnabled("tag_generation")) {
            operations.put("tag_generation", new TagGenerationOperation(client));
        }

        if (config.isOperationEnabled("evaluation")) {
            operations.put("evaluation", new ArticleEvaluationOperation(client));
        }

        return operations;
    }
}