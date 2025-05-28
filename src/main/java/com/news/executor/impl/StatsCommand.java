package com.news.executor.impl;

import com.news.executor.ValidatableCommand;
import com.news.executor.spec.CommandSpec;
import com.news.executor.spec.OptionSpec;
import com.news.model.ArticleStatus;
import com.news.model.ParsedCommand;
import com.news.storage.DatabaseService;

import java.util.Set;

public class StatsCommand implements ValidatableCommand {
    private final DatabaseService databaseService;
    private final CommandSpec commandSpec;

    public StatsCommand(DatabaseService databaseService) {
        this.databaseService = databaseService;
        this.commandSpec = new CommandSpec.Builder()
                .name("stats")
                .description("Display database statistics")
                .options(Set.of(
                        OptionSpec.flag("detailed", "Show detailed statistics by source and status")
                ))
                .build();
    }

    @Override
    public CommandSpec getCommandSpec() {
        return commandSpec;
    }

    @Override
    public void executeValidated(ParsedCommand parsedCommand) {
        boolean showDetailed = parsedCommand.hasOption("detailed");

        System.out.println("=== DATABASE STATISTICS ===");

        // Basic stats
        displayBasicStats();

        if (showDetailed) {
            System.out.println();
            displayDetailedStats();
        }
    }

    private void displayBasicStats() {
        try {
            long totalArticles = databaseService.getArticleRepository().count();
            System.out.println("Total Articles: " + totalArticles);

            // Stats by status
            for (ArticleStatus status : ArticleStatus.values()) {
                long count = databaseService.getArticleRepository().countByStatus(status);
                System.out.println(status.name() + " Articles: " + count);
            }
        } catch (Exception e) {
            System.err.println("Error retrieving statistics: " + e.getMessage());
        }
    }

    private void displayDetailedStats() {
        System.out.println("=== DETAILED STATISTICS ===");
        // Add more detailed statistics here
        // This would require additional repository methods
        System.out.println("(Detailed statistics would be implemented here)");
    }
}