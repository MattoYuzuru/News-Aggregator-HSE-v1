package com.news.executor.impl;

import com.news.executor.Command;
import com.news.model.Article;
import com.news.model.ParsedCommand;
import com.news.model.ParserName;
import com.news.parser.Parser;
import com.news.parser.ParserRegistry;
import com.news.parser.ParserService;
import com.news.storage.DatabaseService;

import java.util.List;

public class ParseCommand implements Command {
    private final DatabaseService databaseService;
    private final ParserRegistry parserRegistry;

    public ParseCommand(DatabaseService databaseService, ParserRegistry parserRegistry) {
        this.databaseService = databaseService;
        this.parserRegistry = parserRegistry;
    }

    @Override
    public void execute(ParsedCommand parsedCommand) {
        if (!parsedCommand.hasOption("source")) {
            throw new IllegalArgumentException("Missing required option '--source'. Use help for more information.");
        }

        String sourceRaw = String.join(" ", parsedCommand.getOptionValues("source"));
        Integer limit = null;

        if (parsedCommand.hasOption("limit")) {
            try {
                limit = Integer.parseInt(parsedCommand.getOption("limit"));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Option '--limit' must be a valid integer.");
            }
        }

        List<Parser> parsers = sourceRaw.equalsIgnoreCase("all")
                ? parserRegistry.getAllParsers()
                : List.of(parserRegistry.getParser(ParserName.valueOf(sourceRaw.toUpperCase())));

        ParserService parserService = new ParserService(parsers, limit);
        List<Article> articles = parserService.collectAllArticlesParallel();

        int savedCount = 0;
        for (Article article : articles) {
            try {
                databaseService.saveArticle(article);
                savedCount++;
            } catch (Exception e) {
                System.err.println("Failed to save article: " + article.getUrl());
                e.printStackTrace();
            }
        }
        System.out.println("Successfully parsed and saved " + savedCount + " articles");
    }
}