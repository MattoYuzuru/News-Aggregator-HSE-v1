package com.news.executor.impl;

import com.news.executor.Command;
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

public class ListCommand implements Command {

    private final DatabaseService databaseService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public ListCommand(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @Override
    public void execute(ParsedCommand parsedCommand) {
        ArticleFilter.Builder filterBuilder = new ArticleFilter.Builder();

        if (parsedCommand.hasOption("source")) {
            filterBuilder.source(String.join(" ", parsedCommand.getOptionValues("source")));
        }

        if (parsedCommand.hasOption("limit")) {
            try {
                filterBuilder.limit(Integer.parseInt(parsedCommand.getOption("limit")));
            } catch (NumberFormatException e) {
                System.err.println("Invalid limit value. Using default.");
            }
        } else {
            filterBuilder.limit(50);
        }

        if (parsedCommand.hasOption("offset")) {
            try {
                filterBuilder.offset(Integer.parseInt(parsedCommand.getOption("offset")));
            } catch (NumberFormatException e) {
                System.err.println("Invalid offset value. Using default.");
            }
        }

        if (parsedCommand.hasOption("status")) {
            try {
                filterBuilder.status(ArticleStatus.valueOf(parsedCommand.getOption("status").toUpperCase()));
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid status value. Valid values are: " +
                        String.join(", ", getStatusNames()));
            }
        }

        if (parsedCommand.hasOption("lang")) {
            filterBuilder.language(parsedCommand.getOption("lang"));
        }

        if (parsedCommand.hasOption("author")) {
            filterBuilder.author(parsedCommand.getOption("author"));
        }

        if (parsedCommand.hasOption("today") && parsedCommand.hasOption("published")) {
            System.err.println("Cannot use both --today and --published options together. Using --today.");
            filterBuilder.todayOnly(true);
        } else if (parsedCommand.hasOption("today")) {
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

        if (parsedCommand.hasOption("tag")) {
            List<String> tags = parsedCommand.getOptionValues("tag");
            if (tags != null && !tags.isEmpty()) {
                filterBuilder.tags(tags);
            }
        }

        if (parsedCommand.hasOption("sort")) {
            String sortField = parsedCommand.getOption("sort");
            filterBuilder.sortBy(validateSortField(sortField));
        }

        if (parsedCommand.hasOption("asc")) {
            filterBuilder.ascending(true);
        }

        ArticleFilter filter = filterBuilder.build();
        List<Article> articles = databaseService.getArticleRepository().findArticlesWithFilters(filter);

        if (articles.isEmpty()) {
            System.out.println("No articles found matching the criteria.");
            return;
        }

        displayArticles(articles);

        if (articles.size() >= filter.getLimit()) {
            int nextOffset = (filter.getOffset() != null ? filter.getOffset() : 0) + filter.getLimit();
            System.out.println("\nShowing " + articles.size() + " articles. For more results, use --offset " + nextOffset);
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

    private void displayArticles(List<Article> articles) {
        System.out.println("Found " + articles.size() + " articles:");
        System.out.println("=========================");

        for (int i = 0; i < articles.size(); i++) {
            Article article = articles.get(i);

            System.out.println((i + 1) + ". " + article.getTitle());
            System.out.println("   Source: " + article.getSourceName());

            if (article.getAuthor() != null && !article.getAuthor().isBlank()) {
                System.out.println("   Author: " + article.getAuthor());
            }

            if (article.getPublishedAt() != null) {
                System.out.println("   Published: " + article.getPublishedAt().format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            }

            System.out.println("   Status: " + article.getStatus());

            if (article.getTags() != null && !article.getTags().isEmpty()) {
                System.out.println("   Tags: " + String.join(", ", article.getTags()));
            }

            System.out.println("   URL: " + article.getUrl());
            System.out.println("--------------------------");
        }
    }
}