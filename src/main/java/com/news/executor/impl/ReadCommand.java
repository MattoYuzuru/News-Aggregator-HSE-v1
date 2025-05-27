package com.news.executor.impl;

import com.news.executor.Command;
import com.news.model.Article;
import com.news.model.ParsedCommand;
import com.news.storage.DatabaseService;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class ReadCommand implements Command {
    private final DatabaseService databaseService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy HH:mm");
    private static final String DIVIDER = "====================================================================";
    private static final String SECTION_DIVIDER = "--------------------------------------------------------------------";

    public ReadCommand(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @Override
    public void execute(ParsedCommand parsedCommand) {
        if (!parsedCommand.hasOption("id")) {
            System.err.println("Error: Missing required parameter --id");
            System.err.println("Usage: read --id ARTICLE_ID [--no-content]");
            return;
        }

        boolean hideContent = parsedCommand.hasOption("no-content");

        long id;
        try {
            id = Long.parseLong(parsedCommand.getOption("id"));
        } catch (NumberFormatException e) {
            System.err.println("Error: Article ID must be a valid number");
            return;
        }

        Optional<Article> articleOpt = databaseService.getArticleRepository().findById(id);

        if (articleOpt.isEmpty()) {
            System.err.println("Error: No article found with ID " + id);
            return;
        }

        displayArticle(articleOpt.get(), hideContent);
    }

    private void displayArticle(Article article, boolean hideContent) {
        System.out.println();
        System.out.println(DIVIDER);

        System.out.println();
        System.out.println("Title: " + article.getTitle());
        System.out.println("Source: " + valueOrUnknown(article.getSourceName()));

        System.out.println(SECTION_DIVIDER);
        System.out.println();
        System.out.println("ARTICLE DETAILS");

        if (article.getPublishedAt() != null) {
            System.out.println("Published: " + article.getPublishedAt().format(DATE_FORMATTER));
        } else {
            System.out.println("Published: Unknown date");
        }

        System.out.println("Author: " + valueOrUnknown(article.getAuthor()));

        if (article.getRegion() != null && !article.getRegion().isBlank()) {
            System.out.println("Region: " + article.getRegion());
        }

        System.out.println("Status: " + article.getStatus());
        System.out.println("Language: " + valueOrUnknown(article.getLanguage()));
        System.out.println("URL: " + article.getUrl());

        if (article.getTags() != null && !article.getTags().isEmpty()) {
            System.out.println("Tags: " + String.join(", ", article.getTags()));
        }

        if (article.getSummary() != null && !article.getSummary().isBlank()) {
            System.out.println();
            System.out.println(SECTION_DIVIDER);
            System.out.println();
            System.out.println("SUMMARY");
            System.out.println(article.getSummary());
        }

        if (!hideContent) {
            System.out.println();
            System.out.println(SECTION_DIVIDER);
            System.out.println();
            System.out.println("CONTENT");
            System.out.println(article.getContent() != null ? article.getContent() : "(No content available)");
        }

        System.out.println();
        System.out.println(DIVIDER);
    }

    private String valueOrUnknown(String value) {
        return (value != null && !value.isBlank()) ? value : "Unknown";
    }
}
