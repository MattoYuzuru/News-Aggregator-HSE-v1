package com.news.executor;

import com.news.model.ParsedCommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandParser {
    public static ParsedCommand parse(String input) {
        String[] parts = input.trim().split("\\s+");
        if (parts.length == 0) {
            throw new IllegalArgumentException("Empty command");
        }

        String name = parts[0];
        Map<String, List<String>> options = new HashMap<>();

        for (int i = 1; i < parts.length; i++) {
            String part = parts[i];

            if (part.startsWith("--")) {
                String key = part.substring(2);

                List<String> values = new ArrayList<>();
                while (i + 1 < parts.length && !parts[i + 1].startsWith("--")) {
                    values.add(parts[++i]);
                }

                if (values.isEmpty()) {
                    options.put(key, List.of("true"));
                } else {
                    options.put(key, values);
                }
            }
        }

        return new ParsedCommand(name, options);
    }
}
