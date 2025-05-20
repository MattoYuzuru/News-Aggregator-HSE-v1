package com.news.executor;

import com.news.model.ParsedCommand;

import java.util.Optional;

public class CommandExecutorService {

    private final CommandRegistry registry;

    public CommandExecutorService(CommandRegistry registry) {
        this.registry = registry;
    }

    public void execute(String input) {
        if (input.isEmpty()) {
            System.out.println("Empty command.");
            return;
        }

        ParsedCommand parsedCommand = CommandParser.parse(input);

        Optional<Command> commandOpt = registry.getCommand(parsedCommand.getName());
        if (commandOpt.isEmpty()) {
            System.out.println("Unknown command: " + parsedCommand.getName());
            return;
        }

        try {
            commandOpt.get().execute(parsedCommand);
        } catch (Exception e) {
            System.out.println("Error executing command: " + e.getMessage());
        }
    }
}
