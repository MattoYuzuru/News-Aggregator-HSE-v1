package com.news.executor;

import com.news.model.ParsedCommand;

import java.util.HashMap;
import java.util.Map;

public class CommandParser {
    public static ParsedCommand parse(String input) {
        String[] parts = input.trim().split("\\s+");
        String name = parts[0];

        Map<String, String> options = new HashMap<>();
        for (int i = 1; i < parts.length; i++) {
            if (parts[i].startsWith("--")) {
                String key = parts[i].substring(2);
                String value = (i + 1 < parts.length && !parts[i + 1].startsWith("--")) ? parts[++i] : "true";
                options.put(key, value);
            }
        }

        return new ParsedCommand(name, options);
    }
}

