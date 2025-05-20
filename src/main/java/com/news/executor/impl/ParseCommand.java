package com.news.executor.impl;

import com.news.executor.Command;
import com.news.model.ParsedCommand;
import com.news.parser.Parser;
import com.news.parser.ParserService;
import com.news.parser.source.BBCParser;
import com.news.parser.source.NHKParser;
import com.news.parser.source.NipponParser;

import java.util.List;
import java.util.Map;

public class ParseCommand implements Command {
    @Override
    public void execute(ParsedCommand parsedCommand) {
        Map<String, String> options = parsedCommand.getOptions();
        String source = options.getOrDefault("source", "all");
        int limit = Integer.parseInt(options.getOrDefault("limit", "10"));

        List<Parser> parserList = getParsers();


        ParserService parserService = new ParserService();
    }

    private static List<Parser> getParsers(String source) {
        if (source.equals("all")) {
            return List.of(
                    new NHKParser(),
                    new NipponParser(),
                    new BBCParser()
            );
        } else {
            return List.of(

            );
        }
    }
}
