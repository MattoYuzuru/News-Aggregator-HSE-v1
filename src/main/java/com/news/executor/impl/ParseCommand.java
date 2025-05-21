package com.news.executor.impl;

import com.news.executor.Command;
import com.news.model.Article;
import com.news.model.ParsedCommand;
import com.news.parser.Parser;
import com.news.parser.ParserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.news.parser.ParserService.AVAILABLE_PARSERS;

public class ParseCommand implements Command {
    @Override
    public void execute(ParsedCommand parsedCommand) {
        Map<String, String> options = parsedCommand.getOptions();

        String sourceRaw = options.getOrDefault("source", "all");
        int limit = Integer.parseInt(options.getOrDefault("limit", "10"));

        List<Parser> parsers = getParsers(sourceRaw);

        ParserService parserService = new ParserService(parsers, limit);
        List<Article> articles = parserService.collectAllArticles();
        System.out.println(articles);
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
