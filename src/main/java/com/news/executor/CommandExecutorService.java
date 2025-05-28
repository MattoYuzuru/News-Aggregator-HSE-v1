package com.news.executor;

import com.news.executor.validation.CommandValidator;
import com.news.executor.validation.ValidationResult;
import com.news.model.ParsedCommand;

public class CommandExecutor {
    private final CommandValidator validator;

    public CommandExecutor() {
        this.validator = new CommandValidator();
    }

    public void execute(ValidatableCommand command, ParsedCommand parsedCommand) {
        ValidationResult result = validator.validate(parsedCommand, command.getCommandSpec());

        if (!result.isValid()) {
            System.err.println("Command validation failed:");
            for (String error : result.getErrors()) {
                System.err.println("  " + error);
            }
            System.err.println();
            printUsage(command);
            return;
        }

        try {
            command.executeValidated(parsedCommand);
        } catch (Exception e) {
            System.err.println("Error executing command: " + e.getMessage());
        }
    }

    private void printUsage(ValidatableCommand command) {
        var spec = command.getCommandSpec();
        System.err.println("Usage: " + spec.getName() + " [OPTIONS]");
        System.err.println(spec.getDescription());
        System.err.println("Options:");

        for (var option : spec.getOptions()) {
            String optionStr = "  --" + option.getName();
            if (option.requiresArgument()) {
                optionStr += " <arg>";
            }
            System.err.println(String.format("%-20s %s", optionStr, option.getDescription()));
        }
    }
}