package com.news.executor.impl;

import com.news.executor.ValidatableCommand;
import com.news.executor.spec.CommandSpec;
import com.news.executor.spec.OptionSpec;
import com.news.model.ParsedCommand;
import com.news.storage.DatabaseService;

import java.sql.SQLException;
import java.util.Set;

public class ClearCommand implements ValidatableCommand {
    private final DatabaseService databaseService;
    private final CommandSpec commandSpec;

    public ClearCommand(DatabaseService databaseService) {
        this.databaseService = databaseService;
        this.commandSpec = new CommandSpec.Builder()
                .name("clear")
                .description("Clear articles from database")
                .options(Set.of(
                        OptionSpec.withSingleArg("id", "Clear article by ID", OptionSpec.OptionType.INTEGER),
                        OptionSpec.flag("all", "Clear all articles")
                ))
                .requiredOptions(Set.of()) // Either id or all is required, handled by mutual exclusion
                .mutuallyExclusiveGroups(Set.of(Set.of("id", "all")))
                .build();
    }

    @Override
    public CommandSpec getCommandSpec() {
        return commandSpec;
    }

    @Override
    public void executeValidated(ParsedCommand parsedCommand) {
        // Validation ensures we have exactly one of id or all
        if (parsedCommand.hasOption("id")) {
            handleClearById(parsedCommand);
        } else if (parsedCommand.hasOption("all")) {
            handleClearAll();
        } else {
            System.err.println("Error: Must use either --id or --all.");
        }
    }

    private void handleClearById(ParsedCommand parsedCommand) {
        try {
            Long id = Long.parseLong(parsedCommand.getOption("id"));
            databaseService.getArticleRepository().deleteById(id);
            System.out.println("Successfully deleted article with ID: " + id);
        } catch (NumberFormatException e) {
            System.err.println("Error: --id argument must be a number.");
        } catch (Exception e) {
            System.err.println("Error deleting article: " + e.getMessage());
        }
    }

    private void handleClearAll() {
        try {
            databaseService.cleanupDatabase();
            System.out.println("Successfully cleaned up the database.");
        } catch (SQLException e) {
            System.err.println("Error while cleaning up the database: " + e.getMessage());
        }
    }
}