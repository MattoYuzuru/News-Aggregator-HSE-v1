package com.news.executor.impl;

import com.news.executor.Command;
import com.news.model.Article;
import com.news.model.ParsedCommand;
import com.news.model.ParserName;
import com.news.parser.Parser;
import com.news.parser.ParserRegistry;
import com.news.parser.ParserService;
import com.news.storage.DatabaseService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class ParseCommand implements Command {
    private final DatabaseService databaseService;
    private final ParserRegistry parserRegistry;

    private static final Set<String> VALID_OPTIONS = Set.of("source", "limit");

    public ParseCommand(DatabaseService databaseService, ParserRegistry parserRegistry) {
        this.databaseService = databaseService;
        this.parserRegistry = parserRegistry;
    }

    @Override
    public void execute(ParsedCommand parsedCommand) {
        for (String option : parsedCommand.getOptions().keySet()) {
            if (!VALID_OPTIONS.contains(option)) {
                System.err.println("Unknown option '--" + option + "'. Allowed options: --source, --limit");
                return;
            }
        }

        if (!parsedCommand.hasOption("source")) {
            System.err.println("Missing required option '--source'.");
            return;
        }

        List<String> sources = parsedCommand.getOptionValues("source");
        List<Parser> parsers = new ArrayList<>();

        if (sources.size() == 1 && sources.getFirst().equalsIgnoreCase("all")) {
            parsers = parserRegistry.getAllParsers();
        } else {
            for (String src : sources) {
                try {
                    ParserName name = ParserName.valueOf(src.toUpperCase());
                    parsers.add(parserRegistry.getParser(name));
                } catch (IllegalArgumentException e) {
                    System.err.println("Warning: unknown parser source '" + src + "'. Skipping...");
                }
            }
        }

        if (parsers.isEmpty()) {
            System.err.println("No valid parsers selected. Use --source with one or more of: " + Arrays.toString(ParserName.values()));
            return;
        }

        Integer limit = null;
        if (parsedCommand.hasOption("limit")) {
            try {
                limit = Integer.parseInt(parsedCommand.getOption("limit"));
            } catch (NumberFormatException e) {
                System.err.println("Invalid value for --limit. Must be an integer.");
                return;
            }
        }

        ParserService parserService = new ParserService(parsers, limit);
        List<Article> articles = parserService.collectAllArticlesParallel();

        int savedCount = 0;
        for (Article article : articles) {
            try {
                databaseService.saveArticle(article);
                savedCount++;
            } catch (Exception e) {
                System.err.println("Failed to save article: " + article.getUrl());
            }
        }

        System.out.println("Successfully parsed and saved " + savedCount + " articles");
    }
}
