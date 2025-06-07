package com.news.ai.config;

import java.util.Set;

public class AIConfiguration {
    private final String modelName;
    private final Set<String> enabledOperations;

    public AIConfiguration(String modelName, Set<String> enabledOperations) {
        this.modelName = modelName;
        this.enabledOperations = enabledOperations;
    }

    public String getModelName() {
        return modelName;
    }

    public Set<String> getEnabledOperations() {
        return enabledOperations;
    }

    public boolean isOperationEnabled(String operationName) {
        return enabledOperations.contains(operationName);
    }
}