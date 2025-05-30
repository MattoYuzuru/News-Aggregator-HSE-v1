package com.news.executor.impl.system.export.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.news.executor.impl.system.export.Exporter;
import com.news.model.Article;

import java.util.List;

public class JsonExporter implements Exporter {
    private final ObjectMapper objectMapper;

    public JsonExporter() {
        this.objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public String export(List<Article> articles) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(articles);
        } catch (Exception e) {
            throw new RuntimeException("Failed to export to JSON", e);
        }
    }
}
