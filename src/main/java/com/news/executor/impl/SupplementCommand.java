package com.news.executor.impl;

import com.news.ai.AIAnalysisService;
import com.news.ai.ArticleAnalyzer;
import com.news.ai.OllamaQwenArticleAnalyzer;
import com.news.executor.ValidatableCommand;
import com.news.executor.spec.CommandSpec;
import com.news.executor.spec.OptionSpec;
import com.news.model.Article;
import com.news.model.ArticleStatus;
import com.news.model.ParsedCommand;
import com.news.storage.DatabaseService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class SupplementCommand implements ValidatableCommand {
    private final DatabaseService databaseService;
    private final CommandSpec commandSpec;

    public SupplementCommand(DatabaseService databaseService) {
        this.databaseService = databaseService;
        this.commandSpec = new CommandSpec.Builder()
                .name("supplement")
                .description("Add AI-generated supplements to articles")
                .options(Set.of(
                        OptionSpec.withSingleArg("id", "Supplement article by ID", OptionSpec.OptionType.INTEGER),
                        OptionSpec.flag("all", "Supplement all ENRICHED articles")
                ))
                .mutuallyExclusiveGroups(Set.of(Set.of("id", "all")))
                .build();
    }

    @Override
    public CommandSpec getCommandSpec() {
        return commandSpec;
    }

    @Override
    public void executeValidated(ParsedCommand parsedCommand) {
        if (!parsedCommand.hasOption("id") && !parsedCommand.hasOption("all")) {
            System.err.println("Error: Must specify either --id or --all");
            return;
        }

        List<Article> articlesToSupplement = new ArrayList<>();

        if (parsedCommand.hasOption("id")) {
            articlesToSupplement = handleSupplementById(parsedCommand);
        } else {
            articlesToSupplement = handleSupplementAll();
        }

        if (articlesToSupplement.isEmpty()) {
            return;
        }

        ArticleAnalyzer articleAnalyzer = new OllamaQwenArticleAnalyzer();
        AIAnalysisService analysisService = new AIAnalysisService(databaseService, articleAnalyzer);

        int savedCount = analysisService.analyzeArticles(articlesToSupplement);
        System.out.println("Successfully supplemented and updated " + savedCount + " articles");
    }

    private List<Article> handleSupplementById(ParsedCommand parsedCommand) {
        try {
            long id = Long.parseLong(parsedCommand.getOption("id"));
            Optional<Article> optionalArticle = databaseService.getArticleRepository().findById(id);

            if (optionalArticle.isEmpty()) {
                System.err.println("No article found with ID: " + id);
                return List.of();
            }

            Article article = optionalArticle.get();
            if (!article.getStatus().equals(ArticleStatus.ENRICHED)) {
                System.err.println("Article has to be enriched before AI supplementation.");
                return List.of();
            }

            return List.of(article);
        } catch (NumberFormatException e) {
            System.err.println("Invalid value for --id. Must be a number.");
            return List.of();
        }
    }

    private List<Article> handleSupplementAll() {
        System.out.println("Fetching all ENRICHED articles");
        List<Article> articles = databaseService.getArticleRepository().findByStatus(ArticleStatus.ENRICHED);

        if (articles.isEmpty()) {
            System.out.println("No articles found for AI enrichment. Run command 'enrich' first to continue.");
            return List.of();
        }

        System.out.println("Found " + articles.size() + " articles for AI enrichment...");
        return articles;
    }
}