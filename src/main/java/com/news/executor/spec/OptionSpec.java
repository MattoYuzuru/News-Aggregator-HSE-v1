package com.news.executor.spec;

import lombok.Getter;

@Getter
public class OptionSpec {
    private final String name;
    private final String description;
    private final boolean requiresArgument;
    private final int minArgs;
    private final int maxArgs;
    private final OptionType type;

    public OptionSpec(String name, String description, boolean requiresArgument,
                      int minArgs, int maxArgs, OptionType type) {
        this.name = name;
        this.description = description;
        this.requiresArgument = requiresArgument;
        this.minArgs = minArgs;
        this.maxArgs = maxArgs;
        this.type = type;
    }

    public boolean requiresArgument() {
        return requiresArgument;
    }

    public static OptionSpec flag(String name, String description) {
        return new OptionSpec(name, description, false, 0, 0, OptionType.FLAG);
    }

    public static OptionSpec withSingleArg(String name, String description, OptionType type) {
        return new OptionSpec(name, description, true, 1, 1, type);
    }

    public static OptionSpec withMultipleArgs(String name, String description, int minArgs, int maxArgs, OptionType type) {
        return new OptionSpec(name, description, true, minArgs, maxArgs, type);
    }

    public enum OptionType {
        FLAG, STRING, INTEGER, DATE, ENUM
    }
}