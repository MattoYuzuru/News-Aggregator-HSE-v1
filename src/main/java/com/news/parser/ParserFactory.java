package com.news.parser;

import com.news.model.ParserName;
import com.news.parser.raw.BBCParser;
import com.news.parser.raw.NHKParser;
import com.news.parser.raw.NipponParser;

import java.util.ArrayList;
import java.util.List;

public class ParserFactory {
    public static Parser getParser(ParserName name) {
        return switch (name) {
            case NHK -> new NHKParser();
            case BBC -> new BBCParser();
            case nippon -> new NipponParser();
        };
    }

    public static List<Parser> getParsers(List<ParserName> names) {
        List<Parser> parsers = new ArrayList<>();
        for (ParserName name : names) {
            parsers.add(getParser(name));
        }
        return parsers;
    }

    public static List<Parser> getAllParsers() {
        List<Parser> parsers = new ArrayList<>();
        for (ParserName name : ParserName.values()) {
            parsers.add(getParser(name));
        }
        return parsers;
    }
}