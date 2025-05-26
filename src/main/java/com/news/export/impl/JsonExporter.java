package com.news.export.impl;

import com.news.export.Exporter;
import com.news.model.Article;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonExporter implements Exporter {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String export(List<Article> articles) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(articles);
        } catch (Exception e) {
            throw new RuntimeException("Failed to export to JSON", e);
        }
    }
}
