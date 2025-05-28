package com.news.executor.impl;

import com.news.executor.ValidatableCommand;
import com.news.executor.spec.CommandSpec;
import com.news.executor.spec.OptionSpec;
import com.news.model.Article;
import com.news.model.ParsedCommand;
import com.news.storage.DatabaseService;

import java.util.List;
import java.util.Set;

public class SearchCommand implements ValidatableCommand {
    private final DatabaseService databaseService;
    private final CommandSpec commandSpec;

    public SearchCommand(DatabaseService databaseService) {
        this.databaseService = databaseService;
        this.commandSpec = new CommandSpec.Builder()
                .name("search")
                .description("Search articles by content, title, or tags")
                .options(Set.of(
                        OptionSpec.withMultipleArgs("content", "Search in article content", 1, Integer.MAX_VALUE, OptionSpec.OptionType.STRING),
                        OptionSpec.withMultipleArgs("title", "Search in article titles", 1, Integer.MAX_VALUE, OptionSpec.OptionType.STRING),
                        OptionSpec.withMultipleArgs("tags", "Search by tags (comma-separated or space-separated)", 1, Integer.MAX_VALUE, OptionSpec.OptionType.STRING)
                ))
                .build();
    }

    @Override
    public CommandSpec getCommandSpec() {
        return commandSpec;
    }

    @Override
    public void executeValidated(ParsedCommand parsedCommand) {
        boolean hasContent = parsedCommand.hasOption("content");
        boolean hasTitle = parsedCommand.hasOption("title");
        boolean hasTags = parsedCommand.hasOption("tags");

        if (!hasContent && !hasTitle && !hasTags) {
            System.err.println("Error: Must specify at least one of --content, --title, or --tags.");
            return;
        }

        List<Article> articles;
        StringBuilder queryDescription = new StringBuilder();

        if (hasContent && hasTitle && hasTags) {
            String contentSubstring = String.join(" ", parsedCommand.getOptionValues("content"));
            String titleSubstring = String.join(" ", parsedCommand.getOptionValues("title"));
            List<String> tagNames = parsedCommand.getOptionValues("tags");

            articles = databaseService.getArticleRepository()
                    .findBySubstrInContentAndTitleAndTags(contentSubstring, titleSubstring, tagNames)
                    .orElse(List.of());
            queryDescription.append("content: \"").append(contentSubstring)
                    .append("\", title: \"").append(titleSubstring)
                    .append("\", tags: ").append(tagNames);
        } else if (hasContent && hasTitle) {
            String contentSubstring = String.join(" ", parsedCommand.getOptionValues(("content")));
            String titleSubstring = String.join(" ", parsedCommand.getOptionValues(("title")));
            articles = databaseService.getArticleRepository()
                    .findBySubstrInContentAndTitle(contentSubstring, titleSubstring)
                    .orElse(List.of());
            queryDescription.append("content: \"").append(contentSubstring)
                    .append("\", title: \"").append(titleSubstring).append("\"");
        } else if (hasContent && hasTags) {
            String contentSubstring = String.join(" ", parsedCommand.getOptionValues(("content")));
            List<String> tagNames = parsedCommand.getOptionValues(("tags"));
            articles = databaseService.getArticleRepository()
                    .findBySubstrInContentAndTags(contentSubstring, tagNames)
                    .orElse(List.of());
            queryDescription.append("content: \"").append(contentSubstring)
                    .append("\", tags: ").append(tagNames);
        } else if (hasTitle && hasTags) {
            String titleSubstring = String.join(" ", parsedCommand.getOptionValues("title"));
            List<String> tagNames = parsedCommand.getOptionValues(("tags"));
            articles = databaseService.getArticleRepository()
                    .findBySubstrInTitleAndTags(titleSubstring, tagNames)
                    .orElse(List.of());
            queryDescription.append("title: \"").append(titleSubstring)
                    .append("\", tags: ").append(tagNames);
        } else if (hasContent) {
            String substring = String.join(" ", parsedCommand.getOptionValues(("content")));
            articles = databaseService.getArticleRepository()
                    .findBySubstrInContent(substring)
                    .orElse(List.of());
            queryDescription.append("\"").append(substring).append("\"");
            printResults(articles, substring, "content");
            return;
        } else if (hasTitle) {
            String substring = String.join(" ", parsedCommand.getOptionValues(("title")));
            articles = databaseService.getArticleRepository()
                    .findBySubstrInTitle(substring)
                    .orElse(List.of());
            queryDescription.append("\"").append(substring).append("\"");
            printResults(articles, substring, "title");
            return;
        } else {
            List<String> tagNames = parsedCommand.getOptionValues(("tags"));
            articles = databaseService.getArticleRepository()
                    .findByTags(tagNames)
                    .orElse(List.of());
            queryDescription.append("tags: ").append(tagNames);
            printResults(articles, tagNames.toString(), "tags");
            return;
        }

        printResults(articles, queryDescription.toString(), "multiple criteria");
    }

    private void printResults(List<Article> articles, String substr, String where) {
        System.out.println("=========================================");
        System.out.println(" Search Results ");
        System.out.println("=========================================");
        System.out.println("Query: " + substr + " (in " + where + ")");
        System.out.println("-----------------------------------------");

        if (articles.isEmpty()) {
            System.out.println("No articles found matching your query.");
        } else {
            System.out.println("Found " + articles.size() + " articles:");
            for (Article article : articles) {
                System.out.println("-----");
                System.out.println("ID:     " + article.getId());
                System.out.println("Title:  " + article.getTitle());
                if (article.getTags() != null && !article.getTags().isEmpty()) {
                    System.out.println("Tags:   " + String.join(", ", article.getTags()));
                }
            }
            System.out.println("-----------------------------------------");
            System.out.println("To read an article, use: read --id <id>");
        }
        System.out.println("=========================================");
    }
}