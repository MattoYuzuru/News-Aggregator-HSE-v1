package com.news.executor;

import com.news.model.ParsedCommand;

import java.util.HashMap;
import java.util.Map;

public class CommandParser {
    public static ParsedCommand parse(String input) {
        String[] parts = input.trim().split("\\s+");
        if (parts.length == 0) {
            throw new IllegalArgumentException("Empty command");
        }

        String name = parts[0];
        Map<String, String> options = new HashMap<>();

        for (int i = 1; i < parts.length; i++) {
            if (parts[i].startsWith("--")) {
                // Extract option name without the -- prefix
                String key = parts[i].substring(2);

                // Check for empty option name
                if (key.isEmpty()) {
                    throw new IllegalArgumentException("Invalid option format. Options must have a name after --");
                }

                // Check if there is a value for this option (not another option / not end of input)
                if (i + 1 < parts.length && !parts[i + 1].startsWith("--")) {
                    // Option has a value
                    String value = parts[++i];
                    options.put(key, value);
                } else {
                    // Option is a flag
                    options.put(key, "true");
                }
            } else {
                // Handle non-option arguments
                throw new IllegalArgumentException("Unexpected argument: " + parts[i] + ". Use --option format for options.");
            }
        }

        return new ParsedCommand(name, options);
    }
}