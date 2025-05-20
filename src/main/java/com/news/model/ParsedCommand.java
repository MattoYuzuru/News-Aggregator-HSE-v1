package com.news.executor;

import lombok.Getter;

import java.util.Map;

@Getter
public class ParsedCommand {
    private final String name;
    private final Map<String, String> options;

    public ParsedCommand(String name, Map<String, String> options) {
        this.name = name;
        this.options = options;
    }
}
