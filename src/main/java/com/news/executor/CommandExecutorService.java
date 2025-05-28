package com.news.executor;

import com.news.executor.validation.CommandValidator;
import com.news.executor.validation.ValidationResult;
import com.news.model.ParsedCommand;
import com.news.executor.impl.ExitCommand;

public class CommandExecutorService {
    private boolean exit = false;
    private final CommandRegistry registry;
    private final CommandValidator validator;

    public CommandExecutorService(CommandRegistry registry) {
        this.registry = registry;
        this.validator = new CommandValidator();
    }

    public void execute(String input) {
        if (input == null || input.isBlank()) {
            System.out.println("Empty command.");
            return;
        }

        try {
            ParsedCommand parsedCommand = CommandParser.parse(input);
            executeCommand(parsedCommand);
        } catch (IllegalArgumentException e) {
            System.err.println("Parse error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
        }
    }

    private void executeCommand(ParsedCommand parsedCommand) {
        registry.getCommand(parsedCommand.getName()).ifPresentOrElse(
                command -> {
                    if (command instanceof ValidatableCommand validatableCommand) {
                        executeWithValidation(validatableCommand, parsedCommand);
                    } else {
                        executeLegacyCommand(command, parsedCommand);
                    }

                    if (command instanceof ExitCommand) {
                        exit = true;
                    }
                },
                () -> {
                    System.err.println("Unknown command: " + parsedCommand.getName());
                    System.err.println("Available commands: " + String.join(", ", registry.getAvailableCommands()));
                }
        );
    }

    private void executeWithValidation(ValidatableCommand command, ParsedCommand parsedCommand) {
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
            System.err.println("Error executing command '" + parsedCommand.getName() + "': " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("Caused by: " + e.getCause().getMessage());
            }
        }
    }

    private void executeLegacyCommand(Command command, ParsedCommand parsedCommand) {
        try {
            command.execute(parsedCommand);
        } catch (Exception e) {
            System.err.println("Error executing command '" + parsedCommand.getName() + "': " + e.getMessage());
        }
    }

    private void printUsage(ValidatableCommand command) {
        var spec = command.getCommandSpec();
        System.err.println("Usage: " + spec.getName() + " [OPTIONS]");
        System.err.println(spec.getDescription());

        if (!spec.getOptions().isEmpty()) {
            System.err.println("Options:");
            for (var option : spec.getOptions()) {
                String optionStr = "  --" + option.getName();
                if (option.requiresArgument()) {
                    if (option.getMaxArgs() == 1) {
                        optionStr += " <value>";
                    } else {
                        optionStr += " <value1> [value2] ...";
                    }
                }
                System.err.printf("%-25s %s%n", optionStr, option.getDescription());
            }
        }

        if (!spec.getRequiredOptions().isEmpty()) {
            System.err.println("Required: " + String.join(", ",
                    spec.getRequiredOptions().stream().map(o -> "--" + o).toList()));
        }
    }

    public boolean shouldExit() {
        return exit;
    }
}