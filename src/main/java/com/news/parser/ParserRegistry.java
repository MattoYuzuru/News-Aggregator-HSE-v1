package com.news.parser;

import com.news.model.ParserName;
import com.news.parser.raw.BBCParser;
import com.news.parser.raw.NHKParser;
import com.news.parser.raw.NipponParser;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ParserRegistry {
    private static final Map<ParserName, Supplier<Parser>> PARSER_SUPPLIERS = Map.of(
            ParserName.NHK, NHKParser::new,
            ParserName.BBC, BBCParser::new,
            ParserName.NIPPON, NipponParser::new
    );

    public List<Parser> getAllParsers() {
        return PARSER_SUPPLIERS.values().stream()
                .map(Supplier::get)
                .toList();
    }

    public Parser getParser(ParserName name) {
        Supplier<Parser> supplier = PARSER_SUPPLIERS.get(name);
        if (supplier == null) {
            throw new IllegalArgumentException("Parser not found for name: " + name);
        }
        return supplier.get();
    }

    public List<ArticleEnricher> getAllEnrichers() {
        return PARSER_SUPPLIERS.values().stream()
                .map(Supplier::get)
                .map(Parser::getEnricher)
                .toList();
    }
}