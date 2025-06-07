package com.news.ai.config;

import java.util.Set;

public record AIConfiguration(String modelName, Set<String> enabledOperations) {

    public boolean isOperationEnabled(String operationName) {
        return enabledOperations.contains(operationName);
    }
}