package com.news.executor.impl;

import com.news.executor.ValidatableCommand;
import com.news.executor.spec.CommandSpec;
import com.news.executor.spec.OptionSpec;
import com.news.model.ParsedCommand;
import com.news.storage.DatabaseService;

import java.sql.SQLException;
import java.util.Set;

public class ExitCommand implements ValidatableCommand {
    private final DatabaseService databaseService;
    private final CommandSpec commandSpec;

    public ExitCommand(DatabaseService databaseService) {
        this.databaseService = databaseService;
        this.commandSpec = new CommandSpec.Builder()
                .name("exit")
                .description("Exit the application")
                .options(Set.of(
                        OptionSpec.flag("delete", "Delete all data before exiting")
                ))
                .build();
    }

    @Override
    public CommandSpec getCommandSpec() {
        return commandSpec;
    }

    @Override
    public void executeValidated(ParsedCommand parsedCommand) {
        if (parsedCommand.hasOption("delete")) {
            try {
                databaseService.cleanupDatabase();
                System.out.println("Database cleaned up successfully.");
            } catch (SQLException e) {
                System.err.println("Error while cleaning up database: " + e.getMessage());
            }
        }

        System.out.println("Goodbye!");
        System.exit(0);
    }
}