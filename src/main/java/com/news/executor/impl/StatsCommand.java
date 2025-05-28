package com.news.executor.impl;

import com.news.executor.ValidatableCommand;
import com.news.executor.spec.CommandSpec;
import com.news.executor.spec.OptionSpec;
import com.news.model.ArticleStatus;
import com.news.model.ParsedCommand;
import com.news.storage.DatabaseService;

import java.util.List;
import java.util.Map;
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
                        OptionSpec.flag("detailed", "Show detailed statistics by source, language, dates, and tags")
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

        System.out.println("📊 === DATABASE STATISTICS ===");
        displayBasicStats();

        if (showDetailed) {
            System.out.println();
            displayDetailedStats();
        }
    }

    private void displayBasicStats() {
        try {
            long totalArticles = databaseService.getArticleRepository().count();
            System.out.printf("📰 Total Articles: %,d%n", totalArticles);

            if (totalArticles > 0) {
                System.out.println("\n📊 By Status:");
                for (ArticleStatus status : ArticleStatus.values()) {
                    long count = databaseService.getArticleRepository().countByStatus(status);
                    double percentage = (count * 100.0) / totalArticles;
                    System.out.printf("   %s: %,d (%.1f%%)%n",
                            getStatusIcon(status) + " " + status.name(), count, percentage);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error retrieving basic statistics: " + e.getMessage());
        }
    }

    private void displayDetailedStats() {
        System.out.println("🔍 === DETAILED STATISTICS ===");

        displaySourceStats();
        displayLanguageStats();
        displayDateRangeStats();
        displayAuthorStats();
        displayTagStats();
        displaySourceStatusBreakdown();
    }

    private void displaySourceStats() {
        try {
            System.out.println("\n🏢 Articles by Source:");
            Map<String, Long> sourceCounts = databaseService.getArticleRepository().countBySource();

            if (sourceCounts.isEmpty()) {
                System.out.println("   No source data available");
                return;
            }

            sourceCounts.entrySet().stream()
                    .limit(10) // Show top 10 sources
                    .forEach(entry ->
                            System.out.printf("   📰 %-20s: %,d articles%n", entry.getKey(), entry.getValue())
                    );

            if (sourceCounts.size() > 10) {
                System.out.printf("   ... and %d more sources%n", sourceCounts.size() - 10);
            }
        } catch (Exception e) {
            System.err.println("   ❌ Error retrieving source statistics: " + e.getMessage());
        }
    }

    private void displayLanguageStats() {
        try {
            System.out.println("\n🌐 Articles by Language:");
            Map<String, Long> languageCounts = databaseService.getArticleRepository().countByLanguage();

            if (languageCounts.isEmpty()) {
                System.out.println("   No language data available");
                return;
            }

            languageCounts.forEach((language, count) ->
                    System.out.printf("   🗣️  %-15s: %,d articles%n", language, count)
            );
        } catch (Exception e) {
            System.err.println("   ❌ Error retrieving language statistics: " + e.getMessage());
        }
    }

    private void displayDateRangeStats() {
        try {
            System.out.println("\n📅 Articles by Time Period:");
            Map<String, Long> dateStats = databaseService.getArticleRepository().getDateRangeStats();

            if (dateStats.isEmpty()) {
                System.out.println("   No date data available");
                return;
            }

            dateStats.forEach((period, count) ->
                    System.out.printf("   ⏰ %-15s: %,d articles%n", period, count)
            );
        } catch (Exception e) {
            System.err.println("   ❌ Error retrieving date statistics: " + e.getMessage());
        }
    }

    private void displayAuthorStats() {
        try {
            System.out.println("\n✍️ Top Authors:");
            List<String> topAuthors = databaseService.getArticleRepository().getTopAuthors(10);

            if (topAuthors.isEmpty()) {
                System.out.println("   No author data available");
                return;
            }

            for (int i = 0; i < topAuthors.size(); i++) {
                System.out.printf("   %d. %s%n", i + 1, topAuthors.get(i));
            }
        } catch (Exception e) {
            System.err.println("   ❌ Error retrieving author statistics: " + e.getMessage());
        }
    }

    private void displayTagStats() {
        try {
            System.out.println("\n🏷️ Top Tags:");
            Map<String, Long> topTags = databaseService.getArticleRepository().getTopTags(15);

            if (topTags.isEmpty()) {
                System.out.println("   No tag data available");
                return;
            }

            topTags.forEach((tag, count) ->
                    System.out.printf("   #%-20s: %,d articles%n", tag, count)
            );
        } catch (Exception e) {
            System.err.println("   ❌ Error retrieving tag statistics: " + e.getMessage());
        }
    }

    private void displaySourceStatusBreakdown() {
        try {
            System.out.println("\n📊 Source Status Breakdown (Top 5 Sources):");
            Map<String, Map<String, Long>> sourceStatusCounts =
                    databaseService.getArticleRepository().countBySourceAndStatus();

            if (sourceStatusCounts.isEmpty()) {
                System.out.println("   No source/status data available");
                return;
            }

            sourceStatusCounts.entrySet().stream()
                    .limit(5)
                    .forEach(sourceEntry -> {
                        System.out.printf("   🏢 %s:%n", sourceEntry.getKey());
                        sourceEntry.getValue().forEach((status, count) ->
                                System.out.printf("      %s %s: %,d%n",
                                        getStatusIcon(ArticleStatus.valueOf(status)), status, count)
                        );
                        System.out.println();
                    });
        } catch (Exception e) {
            System.err.println("   ❌ Error retrieving source/status breakdown: " + e.getMessage());
        }
    }

    private String getStatusIcon(ArticleStatus status) {
        return switch (status) {
            case RAW -> "🔴";
            case ENRICHED -> "🟡";
            case ANALYZED -> "🔵";
            default -> "⚪";
        };
    }
}