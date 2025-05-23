package com.news.executor.impl;

import com.news.executor.Command;
import com.news.model.Article;
import com.news.model.ParsedCommand;
import com.news.parser.Parser;
import com.news.parser.ParserService;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import com.news.storage.DatabaseService;

import static com.news.parser.ParserService.AVAILABLE_PARSERS;

public class ParseCommand implements Command {

    private final DatabaseService databaseService;

    public ParseCommand(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @Override
    public void execute(ParsedCommand parsedCommand) {
        String sourceRaw = "all";
        Integer limit = null;

        System.out.println(parsedCommand.getOptions());

        if (parsedCommand.hasOption("source")) {
            sourceRaw = String.join(" ", parsedCommand.getOptionValues("source"));
        }

        if (parsedCommand.hasOption("limit")) {
            limit = Integer.parseInt(parsedCommand.getOption("limit"));
        }

        if (!parsedCommand.hasOption("source") && !parsedCommand.hasOption("limit")) {
            throw new IllegalArgumentException("Unexpected argument. Use help for options.");
        }

        List<Parser> parsers = getParsers(sourceRaw);

        ParserService parserService = new ParserService(parsers, limit);
        List<Article> articles = parserService.collectAllArticles();
        System.out.println(articles);

        int savedCount = 0;
        for (Article article: articles) {
            try {
                databaseService.saveArticle(article);
                savedCount++;
            } catch (Exception e) {
                System.err.println("Failed to sace article: " + article.getUrl());
                e.printStackTrace();
            }
        }
        System.out.println("Successfully parsed and saved " + savedCount + " articles");
    }

    private static List<Parser> getParsers(String sourceRaw) {
        if (sourceRaw.equalsIgnoreCase("all")) {
            return AVAILABLE_PARSERS.values().stream()
                    .map(Supplier::get)
                    .toList();
        }

        String[] requestedSources = sourceRaw.split("\\s+");
        List<Parser> result = new ArrayList<>();

        for (String source : requestedSources) {
            Supplier<Parser> parserSupplier = AVAILABLE_PARSERS.get(source.toLowerCase());
            if (parserSupplier != null) {
                result.add(parserSupplier.get());
            } else {
                System.err.println("Unknown source: " + source);
            }
        }

        if (result.isEmpty()) {
            throw new IllegalArgumentException("No valid sources provided.");
        }

        return result;
    }
}