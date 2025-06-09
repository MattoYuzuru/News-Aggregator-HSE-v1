package com.news.executor.impl.parsing;

import com.news.executor.ValidatableCommand;
import com.news.executor.spec.CommandSpec;
import com.news.executor.spec.OptionSpec;
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

public class ParseCommand implements ValidatableCommand {
    private final DatabaseService databaseService;
    private final ParserRegistry parserRegistry;
    private final CommandSpec commandSpec;

    public ParseCommand(DatabaseService databaseService, ParserRegistry parserRegistry) {
        this.databaseService = databaseService;
        this.parserRegistry = parserRegistry;
        this.commandSpec = new CommandSpec.Builder()
                .name("parse")
                .description("Parse articles from news sources")
                .options(Set.of(
                        OptionSpec.withMultipleArgs("source", "Source names or 'all' for all sources",
                                1, Integer.MAX_VALUE, OptionSpec.OptionType.STRING),
                        OptionSpec.withSingleArg("limit", "Limit number of articles per source", OptionSpec.OptionType.INTEGER)
                ))
                .requiredOptions(Set.of("source"))
                .build();
    }

    @Override
    public CommandSpec getCommandSpec() {
        return commandSpec;
    }

    @Override
    public void executeValidated(ParsedCommand parsedCommand) {
        List<String> sources = parsedCommand.getOptionValues("source");
        List<Parser> parsers = resolveParsers(sources);

        if (parsers.isEmpty()) {
            System.err.println("No valid parsers selected. Available sources: " +
                    String.join(", ", getAvailableSourceNames()));
            return;
        }

        Integer limit = parseLimit(parsedCommand);

        // execute parsing
        ParserService parserService = new ParserService(parsers, limit);
        List<Article> articles = parserService.collectAllArticlesParallel();

        // save em
        int savedCount = saveArticles(articles);
        System.out.println("Successfully parsed and saved " + savedCount + " articles");
    }

    private List<Parser> resolveParsers(List<String> sources) {
        List<Parser> parsers = new ArrayList<>();

        if (sources.size() == 1 && sources.getFirst().equalsIgnoreCase("all")) {
            return parserRegistry.getAllParsers();
        }

        System.out.println(sources);

        for (String src : sources) {
            try {
                ParserName name = ParserName.valueOf(src.toUpperCase());
                parsers.add(parserRegistry.getParser(name));
            } catch (IllegalArgumentException e) {
                System.err.println("Warning: unknown parser source '" + src + "'. Skipping...");
            }
        }

        return parsers;
    }

    private Integer parseLimit(ParsedCommand parsedCommand) {
        if (!parsedCommand.hasOption("limit")) {
            return null;
        }

        try {
            return Integer.parseInt(parsedCommand.getOption("limit"));
        } catch (NumberFormatException e) {
            System.err.println("Invalid value for --limit. Must be an integer. Ignoring limit.");
            return null;
        }
    }

    private int saveArticles(List<Article> articles) {
        int savedCount = 0;
        for (Article article : articles) {
            try {
                databaseService.saveArticle(article);
                savedCount++;
            } catch (Exception e) {
                System.err.println("Failed to save article: " + article.getUrl());
            }
        }
        return savedCount;
    }

    private String[] getAvailableSourceNames() {
        return Arrays.stream(ParserName.values())
                .map(Enum::name)
                .map(String::toLowerCase)
                .toArray(String[]::new);
    }
}