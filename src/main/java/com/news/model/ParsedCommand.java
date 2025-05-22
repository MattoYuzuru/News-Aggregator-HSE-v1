package com.news.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class ParsedCommand {
    private final String name;
    private final Map<String, List<String>> options;

    public ParsedCommand(String name, Map<String, List<String>> options) {
        this.name = name;
        this.options = options;
    }

    public String getOption(String key) {
        List<String> values = options.get(key);
        return values != null && !values.isEmpty() ? values.getFirst() : null;
    }

    public List<String> getOptionValues(String key) {
        return options.get(key);
    }

    public boolean hasOption(String key) {
        return options.containsKey(key);
    }
}