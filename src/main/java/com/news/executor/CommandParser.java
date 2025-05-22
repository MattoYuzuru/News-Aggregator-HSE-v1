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
            if (parts[i].startsWith("--")) {
                // Extract option name without the -- prefix
                String key = parts[i].substring(2);

                // Check for empty option name
                if (key.isEmpty()) {
                    throw new IllegalArgumentException("Invalid option format. Options must have a name after --");
                }

                List<String> values = new ArrayList<>();

                // Collect all subsequent values until next option or end
                while (i + 1 < parts.length && !parts[i + 1].startsWith("--")) {
                    values.add(parts[++i]);
                }

                if (values.isEmpty()) {
                    // Option is a flag with no values
                    options.put(key, List.of("true"));
                } else {
                    // Option with one or more values
                    options.put(key, values);
                }
            } else {
                // Handle non-option arguments
                throw new IllegalArgumentException("Unexpected argument: " + parts[i] + ". Use --option format for options.");
            }
        }

        return new ParsedCommand(name, options);
    }
}