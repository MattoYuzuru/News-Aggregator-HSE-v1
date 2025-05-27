package com.news.parser;

import com.news.model.Article;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ParserService {
    private final List<Parser> parsers;
    private final Integer limit;

    public ParserService(List<Parser> parsers, Integer limit) {
        this.parsers = parsers;
        this.limit = limit;
    }

    public List<Article> collectAllArticles() {
        List<Article> all = new ArrayList<>();

        if (parsers.isEmpty()) {
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

        int totalParsers = parsers.size();
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

    public List<Article> collectAllArticlesParallel() {
        List<Article> all = Collections.synchronizedList(new ArrayList<>());

        ExecutorService executor = Executors.newFixedThreadPool(parsers.size());
        List<Future<?>> futures = new ArrayList<>();

        for (Parser parser : parsers) {
            futures.add(executor.submit(() -> {
                List<Article> articles = parser.fetchArticles();
                if (limit != null && limit > 0) {
                    articles = articles.stream().limit(limit / parsers.size()).toList();
                }
                all.addAll(articles);
            }));
        }

        for (Future<?> f : futures) {
            try {
                f.get(); // wait for all
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();
        return all;
    }
}