package com.news.executor.impl;

import com.news.executor.Command;
import com.news.model.Article;
import com.news.model.ParsedCommand;
import com.news.storage.DatabaseService;

import java.util.List;

public class SearchCommand implements Command {
    private final DatabaseService databaseService;

    public SearchCommand(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @Override
    public void execute(ParsedCommand parsedCommand) {
        boolean hasContent = parsedCommand.hasOption("content");
        boolean hasTitle = parsedCommand.hasOption("title");

        String substring;
        List<Article> articles;

        if (hasContent == hasTitle) {
            throw new IllegalArgumentException(
                    """
                            \n====================
                            Invalid command usage!
                            You must provide exactly one search option:
                              --content <substring>   (search in article content)
                              --title <substring>     (search in article titles)
                            Example: search --content "climate change"
                            ===================="""
            );
        }

        if (hasContent) {
            substring = parsedCommand.getOption("content");
            articles = databaseService.getArticleRepository()
                    .findBySubstrInContent(substring)
                    .orElse(List.of());
            printResults(articles, substring, "content");
        } else {
            substring = parsedCommand.getOption("title");
            articles = databaseService.getArticleRepository()
                    .findBySubstrInTitle(substring)
                    .orElse(List.of());
            printResults(articles, substring, "title");
        }
    }

    private void printResults(List<Article> articles, String substr, String where) {
        System.out.println("=========================================");
        System.out.println(" Search Results ");
        System.out.println("=========================================");
        System.out.println("Query: \"" + substr + "\" (in " + where + ")");
        System.out.println("-----------------------------------------");

        if (articles.isEmpty()) {
            System.out.println("No articles found matching your query.");
        } else {
            System.out.println("Found " + articles.size() + " articles:");
            for (Article article : articles) {
                System.out.println("-----");
                System.out.println("ID:     " + article.getId());
                System.out.println("Title:  " + article.getTitle());
            }
            System.out.println("-----------------------------------------");
            System.out.println("To read an article, use: read --id <id>");
        }
        System.out.println("=========================================");
    }
}