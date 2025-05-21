package com.news.parser;

import com.news.model.Article;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.news.parser.source.*;

public class ParserService {
    private final List<Parser> parsers;
    private final Integer limit;
    public static final Map<String, Supplier<Parser>> AVAILABLE_PARSERS = Map.of(
            "nhk", NHKParser::new,
            "nippon", NipponParser::new,
            "bbc", BBCParser::new
    );

    public ParserService(List<Parser> parsers, Integer limit) {
        this.parsers = parsers;
        this.limit = limit;
    }

    public ParserService(List<Parser> parsers) {
        this.parsers = parsers;
        this.limit = null;
    }

    public List<Article> collectAllArticles() {
        List<Article> all = new ArrayList<>();

        int totalParsers = parsers.size();
        if (totalParsers == 0) {
            return all;
        }

        if (limit == null) {
            for (Parser parser : parsers) {
                all.addAll(parser.fetchArticles());
            }
            return all;
        }

        if (limit <= 0) {
            return all;
        }

        int baseLimit = limit / totalParsers;
        int remainder = limit % totalParsers;

        for (int i = 0; i < parsers.size(); i++) {
            Parser parser = parsers.get(i);
            int currentLimit = baseLimit + (i < remainder ? 1 : 0);

            List<Article> articles = parser.fetchArticles().stream()
                    .limit(currentLimit)
                    .toList();

            all.addAll(articles);
        }

        return all;
    }
}