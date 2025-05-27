package com.news.executor;

import com.news.model.ParsedCommand;
import com.news.executor.impl.ExitCommand;
public class CommandExecutorService {

    private boolean exit = false;
    private final CommandRegistry registry;

    public CommandExecutorService(CommandRegistry registry) {
        this.registry = registry;
    }

    public void execute(String input) {
        if (input == null || input.isBlank()) {
            System.out.println("Empty command.");
            return;
        }

        ParsedCommand parsedCommand = CommandParser.parse(input);

        registry.getCommand(parsedCommand.getName()).ifPresentOrElse(
                command -> {
                    try {
                        command.execute(parsedCommand);
                        if (command instanceof ExitCommand) {
                            exit = true;
                        }
                    } catch (Exception e) {
                        System.out.println("Error executing command '" + parsedCommand.getName() + "': " + e.getMessage());
                    }
                },
                () -> System.out.println("Unknown command: " + parsedCommand.getName())
        );
    }

    public boolean shouldExit() {
        return exit;
    }
}
