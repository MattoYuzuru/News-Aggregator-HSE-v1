package com.news.executor.impl;

import com.news.executor.ValidatableCommand;
import com.news.executor.spec.CommandSpec;
import com.news.executor.spec.OptionSpec;
import com.news.model.Article;
import com.news.model.ParsedCommand;
import com.news.storage.DatabaseService;

import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Set;

public class ReadCommand implements ValidatableCommand {
    private final DatabaseService databaseService;
    private final CommandSpec commandSpec;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy HH:mm");
    private static final String DIVIDER = "====================================================================";
    private static final String SECTION_DIVIDER = "--------------------------------------------------------------------";
    private static final int MAX_LINE_LENGTH = 80;

    public ReadCommand(DatabaseService databaseService) {
        this.databaseService = databaseService;
        this.commandSpec = new CommandSpec.Builder()
                .name("read")
                .description("Read an article by ID")
                .options(Set.of(
                        OptionSpec.withSingleArg("id", "Article ID to read", OptionSpec.OptionType.INTEGER),
                        OptionSpec.flag("no-content", "Hide article content")
                ))
                .requiredOptions(Set.of("id"))
                .build();
    }

    @Override
    public CommandSpec getCommandSpec() {
        return commandSpec;
    }

    @Override
    public void executeValidated(ParsedCommand parsedCommand) {
        boolean hideContent = parsedCommand.hasOption("no-content");

        try {
            long id = Long.parseLong(parsedCommand.getOption("id"));
            Optional<Article> articleOpt = databaseService.getArticleRepository().findById(id);

            if (articleOpt.isEmpty()) {
                System.err.println("Error: No article found with ID " + id);
                return;
            }

            displayArticle(articleOpt.get(), hideContent);
        } catch (NumberFormatException e) {
            System.err.println("Error: Article ID must be a valid number");
        }
    }

    private void displayArticle(Article article, boolean hideContent) {
        System.out.println();
        System.out.println(DIVIDER);

        // Header section
        System.out.println();
        System.out.printf("📰 %s%n", article.getTitle());
        System.out.printf("🏢 Source: %s%n", valueOrUnknown(article.getSourceName()));

        System.out.println(SECTION_DIVIDER);
        System.out.println();
        System.out.println("📋 ARTICLE DETAILS");

        if (article.getPublishedAt() != null) {
            System.out.printf("📅 Published: %s%n", article.getPublishedAt().format(DATE_FORMATTER));
        } else {
            System.out.println("📅 Published: Unknown date");
        }

        System.out.printf("✍️  Author: %s%n", valueOrUnknown(article.getAuthor()));

        if (article.getRegion() != null && !article.getRegion().isBlank()) {
            System.out.printf("🌍 Region: %s%n", article.getRegion());
        }

        System.out.printf("📊 Status: %s%n", article.getStatus());
        System.out.printf("🌐 Language: %s%n", valueOrUnknown(article.getLanguage()));
        System.out.printf("🔗 URL: %s%n", article.getUrl());

        displayTags(article);

        if (article.getSummary() != null && !article.getSummary().isBlank()) {
            System.out.println();
            System.out.println(SECTION_DIVIDER);
            System.out.println();
            System.out.println("📝 SUMMARY");
            System.out.println();
            displayFormattedText(article.getSummary());
        }

        if (!hideContent) {
            System.out.println();
            System.out.println(SECTION_DIVIDER);
            System.out.println();
            System.out.println("📖 CONTENT");
            System.out.println();

            if (article.getContent() != null && !article.getContent().isBlank()) {
                displayFormattedContent(article.getContent());
            } else {
                System.out.println("   (No content available)");
            }
        }

        System.out.println();
        System.out.println(DIVIDER);
        System.out.println();
    }

    private void displayTags(Article article) {
        if (article.getTags() == null || article.getTags().isEmpty()) {
            System.out.println("🏷️  Tags: [No tags]");
            return;
        }

        System.out.print("🏷️  Tags: ");
        for (int i = 0; i < article.getTags().size(); i++) {
            System.out.print("#" + article.getTags().get(i));
            if (i < article.getTags().size() - 1) {
                System.out.print("  ");
            }
        }
        System.out.println();
    }

    private void displayFormattedContent(String content) {
        if (content == null || content.isBlank()) {
            System.out.println("   (No content available)");
            return;
        }

        String cleanedContent = cleanContent(content);

        String[] paragraphs = cleanedContent.split("\n\n|\r\n\r\n");

        for (int i = 0; i < paragraphs.length; i++) {
            String paragraph = paragraphs[i].trim();
            if (!paragraph.isEmpty()) {
                displayParagraph(paragraph);

                if (i < paragraphs.length - 1) {
                    System.out.println();
                }
            }
        }
    }

    private void displayFormattedText(String text) {
        if (text == null || text.isBlank()) {
            return;
        }

        String cleanedText = cleanContent(text);
        displayParagraph(cleanedText);
    }

    private String cleanContent(String content) {
        if (content == null) return "";

        return content
                // Remove HTML tags
                .replaceAll("<[^>]+>", "")
                // Clean up multiple spaces
                .replaceAll(" +", " ")
                // Clean up multiple line breaks
                .replaceAll("\n+", "\n")
                // Remove leading/trailing whitespace
                .trim();
    }

    private void displayParagraph(String paragraph) {
        if (paragraph.length() <= MAX_LINE_LENGTH) {
            System.out.println("   " + paragraph);
            return;
        }

        String[] words = paragraph.split("\\s+");
        StringBuilder currentLine = new StringBuilder("   ");

        for (String word : words) {
            // Check if adding this word would exceed the line length
            if (currentLine.length() + word.length() + 1 > MAX_LINE_LENGTH) {
                // Print current line and start a new one
                System.out.println(currentLine);
                currentLine = new StringBuilder("   " + word);
            } else {
                // Add word to current line
                if (currentLine.length() > 3) { // More than just the indent
                    currentLine.append(" ");
                }
                currentLine.append(word);
            }
        }

        // Print the last line if it has content
        if (currentLine.length() > 3) {
            System.out.println(currentLine);
        }
    }

    private String valueOrUnknown(String value) {
        return (value != null && !value.isBlank()) ? value : "Unknown";
    }
}