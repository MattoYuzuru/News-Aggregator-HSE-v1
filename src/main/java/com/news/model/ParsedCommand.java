package com.news.model;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ParsedCommand {
    private final String name;
    private final Map<String, String> options;

    public ParsedCommand(String name, Map<String, String> options) {
        this.name = name;
        this.options = options;
    }
}
