package com.news.executor.impl.manip;

import com.news.executor.ValidatableCommand;
import com.news.executor.spec.CommandSpec;
import com.news.executor.spec.OptionSpec;
import com.news.model.Article;
import com.news.model.ArticleFilter;
import com.news.model.ArticleStatus;
import com.news.model.ParsedCommand;
import com.news.storage.DatabaseService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Set;

public class ListCommand implements ValidatableCommand {
    private final DatabaseService databaseService;
    private final CommandSpec commandSpec;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public ListCommand(DatabaseService databaseService) {
        this.databaseService = databaseService;
        this.commandSpec = new CommandSpec.Builder()
                .name("list")
                .description("List articles with filtering and sorting options")
                .options(Set.of(
                        OptionSpec.withSingleArg("source", "Filter by source", OptionSpec.OptionType.STRING),
                        OptionSpec.withSingleArg("limit", "Limit number of results (default: 10)", OptionSpec.OptionType.INTEGER),
                        OptionSpec.withSingleArg("offset", "Offset for pagination", OptionSpec.OptionType.INTEGER),
                        OptionSpec.withSingleArg("status", "Filter by status (RAW, ENRICHED, SUPPLEMENTED)", OptionSpec.OptionType.STRING),
                        OptionSpec.withSingleArg("lang", "Filter by language", OptionSpec.OptionType.STRING),
                        OptionSpec.withMultipleArgs("author", "Filter by author", 1, Integer.MAX_VALUE, OptionSpec.OptionType.STRING),
                        OptionSpec.flag("today", "Show only today's articles"),
                        OptionSpec.withSingleArg("published", "Filter by published date (DD-MM-YYYY)", OptionSpec.OptionType.DATE),
                        OptionSpec.withMultipleArgs("tag", "Filter by tags", 1, Integer.MAX_VALUE, OptionSpec.OptionType.STRING),
                        OptionSpec.withSingleArg("sort", "Sort field (date, title, source, author)", OptionSpec.OptionType.STRING),
                        OptionSpec.flag("asc", "Sort in ascending order")
                ))
                .mutuallyExclusiveGroups(Set.of(Set.of("today", "published")))
                .build();
    }

    @Override
    public CommandSpec getCommandSpec() {
        return commandSpec;
    }

    @Override
    public void executeValidated(ParsedCommand parsedCommand) {
        ArticleFilter.Builder filterBuilder = new ArticleFilter.Builder();

        applySourceFilter(parsedCommand, filterBuilder);
        applyLimitAndOffset(parsedCommand, filterBuilder);
        applyStatusFilter(parsedCommand, filterBuilder);
        applyLanguageFilter(parsedCommand, filterBuilder);
        applyAuthorFilter(parsedCommand, filterBuilder);
        applyDateFilters(parsedCommand, filterBuilder);
        applyTagFilters(parsedCommand, filterBuilder);
        applySortingOptions(parsedCommand, filterBuilder);

        ArticleFilter filter = filterBuilder.build();

        printFilterSummary(filter);

        List<Article> articles = databaseService.getArticleRepository().findArticlesWithFilters(filter);

        displayResults(articles, filter);
    }

    private void applySourceFilter(ParsedCommand parsedCommand, ArticleFilter.Builder filterBuilder) {
        if (parsedCommand.hasOption("source")) {
            String source = parsedCommand.getOption("source");
            filterBuilder.source(source.toUpperCase());
        }
    }

    private void applyLimitAndOffset(ParsedCommand parsedCommand, ArticleFilter.Builder filterBuilder) {
        if (parsedCommand.hasOption("limit")) {
            try {
                filterBuilder.limit(Integer.parseInt(parsedCommand.getOption("limit")));
            } catch (NumberFormatException e) {
                System.err.println("Invalid limit value. Using default (10).");
                filterBuilder.limit(10);
            }
        } else {
            filterBuilder.limit(10);
        }

        if (parsedCommand.hasOption("offset")) {
            try {
                filterBuilder.offset(Integer.parseInt(parsedCommand.getOption("offset")));
            } catch (NumberFormatException e) {
                System.err.println("Invalid offset value. Using default (0).");
                filterBuilder.offset(0);
            }
        }
    }

    private void applyStatusFilter(ParsedCommand parsedCommand, ArticleFilter.Builder filterBuilder) {
        if (parsedCommand.hasOption("status")) {
            try {
                String statusValue = parsedCommand.getOption("status").toUpperCase();
                filterBuilder.status(ArticleStatus.valueOf(statusValue));
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid status value: " + parsedCommand.getOption("status") +
                        ". Valid values are: " + String.join(", ", getStatusNames()));
            }
        }
    }

    private void applyLanguageFilter(ParsedCommand parsedCommand, ArticleFilter.Builder filterBuilder) {
        if (parsedCommand.hasOption("lang")) {
            filterBuilder.language(parsedCommand.getOption("lang"));
        }
    }

    private void applyAuthorFilter(ParsedCommand parsedCommand, ArticleFilter.Builder filterBuilder) {
        if (parsedCommand.hasOption("author")) {
            filterBuilder.author(String.join(" ", parsedCommand.getOptionValues("author")));
        }
    }

    private void applyDateFilters(ParsedCommand parsedCommand, ArticleFilter.Builder filterBuilder) {
        if (parsedCommand.hasOption("today")) {
            filterBuilder.todayOnly(true);
        } else if (parsedCommand.hasOption("published")) {
            try {
                String dateStr = parsedCommand.getOption("published");
                LocalDate date = LocalDate.parse(dateStr, DATE_FORMATTER);
                LocalDateTime startOfDay = date.atStartOfDay();
                LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

                filterBuilder.publishedAfter(startOfDay);
                filterBuilder.publishedBefore(endOfDay);
            } catch (DateTimeParseException e) {
                System.err.println("Invalid date format. Use DD-MM-YYYY format.");
            }
        }
    }

    private void applyTagFilters(ParsedCommand parsedCommand, ArticleFilter.Builder filterBuilder) {
        if (parsedCommand.hasOption("tag")) {
            List<String> tagValues = parsedCommand.getOptionValues("tag");
            if (tagValues != null && !tagValues.isEmpty()) {
                filterBuilder.tags(tagValues);
            }
        }
    }

    private void applySortingOptions(ParsedCommand parsedCommand, ArticleFilter.Builder filterBuilder) {
        if (parsedCommand.hasOption("sort")) {
            String sortField = parsedCommand.getOption("sort");
            filterBuilder.sortBy(validateSortField(sortField));
        }

        if (parsedCommand.hasOption("asc")) {
            filterBuilder.ascending(true);
        }
    }

    private String validateSortField(String field) {
        return switch (field.toLowerCase()) {
            case "date", "published", "published_at" -> "published_at";
            case "title" -> "title";
            case "source" -> "source_name";
            case "author" -> "author";
            default -> {
                System.err.println("Invalid sort field: " + field + ". Using published_at.");
                yield "published_at";
            }
        };
    }

    private String[] getStatusNames() {
        return java.util.Arrays.stream(ArticleStatus.values())
                .map(Enum::name)
                .toArray(String[]::new);
    }

    private void printFilterSummary(ArticleFilter filter) {
        System.out.println("🔍 Applied Filters:");
        if (filter.getStatus() != null) {
            System.out.println("   📊 Status: " + filter.getStatus());
        }
        if (filter.getSource() != null) {
            System.out.println("   🏢 Source: " + filter.getSource());
        }
        if (filter.getAuthor() != null) {
            System.out.println("   ✍️  Author: " + filter.getAuthor());
        }
        if (filter.getLanguage() != null) {
            System.out.println("   🌐 Language: " + filter.getLanguage());
        }
        if (filter.getTags() != null && !filter.getTags().isEmpty()) {
            System.out.println("   🏷️  Tags: " + String.join(", ", filter.getTags()));
        }
        if (filter.isTodayOnly()) {
            System.out.println("   📅 Date: Today only");
        } else if (filter.getPublishedAfter() != null || filter.getPublishedBefore() != null) {
            System.out.println("   📅 Date range: " +
                    (filter.getPublishedAfter() != null ? filter.getPublishedAfter().toLocalDate() : "any") +
                    " to " +
                    (filter.getPublishedBefore() != null ? filter.getPublishedBefore().toLocalDate() : "any"));
        }
        System.out.println("   📄 Limit: " + filter.getLimit() +
                (filter.getOffset() != null && filter.getOffset() > 0 ? " (offset: " + filter.getOffset() + ")" : ""));
        System.out.println("   🔄 Sort: " + filter.getSortBy() + (filter.isAscending() ? " (asc)" : " (desc)"));
        System.out.println();
    }

    private void displayResults(List<Article> articles, ArticleFilter filter) {
        if (articles.isEmpty()) {
            System.out.println("❌ No articles found matching the criteria.");
            return;
        }

        displayArticles(articles);

        if (articles.size() >= filter.getLimit()) {
            int nextOffset = (filter.getOffset() != null ? filter.getOffset() : 0) + filter.getLimit();
            System.out.println("\n💡 Showing " + articles.size() + " articles. For more results, use --offset " + nextOffset);
        } else {
            System.out.println("\n✅ Showing all " + articles.size() + " matching articles.");
        }
    }

    private void displayArticles(List<Article> articles) {
        System.out.println("📋 Found " + articles.size() + " articles:");
        System.out.println("═══════════════════════════════════════════════════════════════════════════════");

        for (int i = 0; i < articles.size(); i++) {
            Article article = articles.get(i);

            System.out.printf("📰 %d. %s%n", (i + 1), article.getTitle());
            System.out.printf("   🆔 ID: %d%n", article.getId());
            System.out.printf("   😃 Relevancy rate: %d%n", article.getRating());
            System.out.printf("   🏢 Source: %s%n", article.getSourceName() != null ? article.getSourceName() : "Unknown");

            if (article.getAuthor() != null && !article.getAuthor().isBlank()) {
                System.out.printf("   ✍️  Author: %s%n", article.getAuthor());
            }

            if (article.getPublishedAt() != null) {
                System.out.printf("   📅 Published: %s%n", article.getPublishedAt().format(
                        DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));
            }

            System.out.printf("   📊 Status: %s%n", article.getStatus());

            if (article.getTags() != null && !article.getTags().isEmpty()) {
                System.out.printf("   🏷️  Tags: %s%n", String.join(", ", article.getTags()));
            }

            if (article.getLanguage() != null && !article.getLanguage().isBlank()) {
                System.out.printf("   🌐 Language: %s%n", article.getLanguage());
            }

            System.out.printf("   🔗 URL: %s%n", article.getUrl());
            System.out.println("───────────────────────────────────────────────────────────────────────────────");
        }
    }
}