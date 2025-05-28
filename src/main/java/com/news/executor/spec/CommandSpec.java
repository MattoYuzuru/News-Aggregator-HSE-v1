package com.news.executor.spec;

import lombok.Getter;

import java.util.Set;

@Getter
public class CommandSpec {
    private final String name;
    private final String description;
    private final Set<OptionSpec> options;
    private final Set<String> requiredOptions;
    private final Set<Set<String>> mutuallyExclusiveGroups;

    public CommandSpec(String name, String description, Set<OptionSpec> options,
                       Set<String> requiredOptions, Set<Set<String>> mutuallyExclusiveGroups) {
        this.name = name;
        this.description = description;
        this.options = options;
        this.requiredOptions = requiredOptions;
        this.mutuallyExclusiveGroups = mutuallyExclusiveGroups;
    }

    public static class Builder {
        private String name;
        private String description;
        private Set<OptionSpec> options = Set.of();
        private Set<String> requiredOptions = Set.of();
        private Set<Set<String>> mutuallyExclusiveGroups = Set.of();

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder options(Set<OptionSpec> options) {
            this.options = options;
            return this;
        }

        public Builder requiredOptions(Set<String> requiredOptions) {
            this.requiredOptions = requiredOptions;
            return this;
        }

        public Builder mutuallyExclusiveGroups(Set<Set<String>> groups) {
            this.mutuallyExclusiveGroups = groups;
            return this;
        }

        public CommandSpec build() {
            return new CommandSpec(name, description, options, requiredOptions, mutuallyExclusiveGroups);
        }
    }
}