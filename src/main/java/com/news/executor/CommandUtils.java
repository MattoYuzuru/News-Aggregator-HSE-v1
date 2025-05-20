package com.news.executor;

import java.util.HashMap;
import java.util.Map;

public class CommandUtils {

    public static Map<String, String> parseArgs(String[] args) {
        Map<String, String> result = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("--")) {
                String key = args[i].substring(2);
                String value = "true";
                if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                    value = args[++i];
                }
                result.put(key, value);
            }
        }
        return result;
    }
}
