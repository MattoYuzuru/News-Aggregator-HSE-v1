package com.news.executor.validation;

import com.news.executor.spec.CommandSpec;
import com.news.executor.spec.OptionSpec;
import com.news.model.ParsedCommand;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class CommandValidator {

    public ValidationResult validate(ParsedCommand parsedCommand, CommandSpec spec) {
        List<String> errors = new ArrayList<>();

        validateUnknownOptions(parsedCommand, spec, errors);

        validateRequiredOptions(parsedCommand, spec, errors);

        validateMutuallyExclusiveGroups(parsedCommand, spec, errors);

        validateOptionArguments(parsedCommand, spec, errors);

        return new ValidationResult(errors.isEmpty(), errors);
    }

    private void validateUnknownOptions(ParsedCommand parsedCommand, CommandSpec spec, List<String> errors) {
        Set<String> validOptions = spec.getOptions().stream()
                .map(OptionSpec::getName)
                .collect(Collectors.toSet());

        for (String option : parsedCommand.getOptions().keySet()) {
            if (!validOptions.contains(option)) {
                errors.add(String.format("Unknown option '--%s'. Valid options: %s",
                        option, String.join(", ", validOptions.stream().map(o -> "--" + o).toList())));
            }
        }
    }

    private void validateRequiredOptions(ParsedCommand parsedCommand, CommandSpec spec, List<String> errors) {
        for (String required : spec.getRequiredOptions()) {
            if (!parsedCommand.hasOption(required)) {
                errors.add(String.format("Missing required option '--%s'", required));
            }
        }
    }

    private void validateMutuallyExclusiveGroups(ParsedCommand parsedCommand, CommandSpec spec, List<String> errors) {
        for (Set<String> group : spec.getMutuallyExclusiveGroups()) {
            List<String> presentOptions = group.stream()
                    .filter(parsedCommand::hasOption)
                    .toList();

            if (presentOptions.size() > 1) {
                errors.add(String.format("Cannot use options together: %s",
                        String.join(", ", presentOptions.stream().map(o -> "--" + o).toList())));
            }
        }
    }

    private void validateOptionArguments(ParsedCommand parsedCommand, CommandSpec spec, List<String> errors) {
        for (OptionSpec optionSpec : spec.getOptions()) {
            if (parsedCommand.hasOption(optionSpec.getName())) {
                List<String> values = parsedCommand.getOptionValues(optionSpec.getName());
                int valueCount = values != null ? values.size() : 0;

                if (optionSpec.requiresArgument()) {
                    if (valueCount < optionSpec.getMinArgs()) {
                        errors.add(String.format("Option '--%s' requires at least %d argument(s), got %d",
                                optionSpec.getName(), optionSpec.getMinArgs(), valueCount));
                    }
                    if (valueCount > optionSpec.getMaxArgs()) {
                        errors.add(String.format("Option '--%s' accepts at most %d argument(s), got %d",
                                optionSpec.getName(), optionSpec.getMaxArgs(), valueCount));
                    }
                } else {
                    if (values != null && !values.equals(List.of("true"))) {
                        errors.add(String.format("Flag '--%s' should not have arguments", optionSpec.getName()));
                    }
                }

                validateArgumentTypes(optionSpec, values, errors);
            }
        }
    }

    private void validateArgumentTypes(OptionSpec optionSpec, List<String> values, List<String> errors) {
        if (values == null || values.isEmpty()) return;

        if (Objects.requireNonNull(optionSpec.getType()) == OptionSpec.OptionType.INTEGER) {
            for (String value : values) {
                try {
                    Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    errors.add(String.format("Option '--%s' requires integer value, got '%s'",
                            optionSpec.getName(), value));
                }
            }
        }
    }
}