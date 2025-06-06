package com.news.executor.impl.parsing;

import com.news.executor.ValidatableCommand;
import com.news.executor.spec.CommandSpec;
import com.news.executor.spec.OptionSpec;
import com.news.model.Article;
import com.news.model.ArticleStatus;
import com.news.model.ParsedCommand;
import com.news.parser.ArticleEnricher;
import com.news.parser.EnrichmentService;
import com.news.parser.ParserRegistry;
import com.news.storage.DatabaseService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class EnrichCommand implements ValidatableCommand {
    private final DatabaseService databaseService;
    private final ParserRegistry parserRegistry;
    private final CommandSpec commandSpec;

    public EnrichCommand(DatabaseService databaseService, ParserRegistry parserRegistry) {
        this.databaseService = databaseService;
        this.parserRegistry = parserRegistry;
        this.commandSpec = new CommandSpec.Builder()
                .name("enrich")
                .description("Enrich articles with additional data")
                .options(Set.of(
                        OptionSpec.withSingleArg("id", "Enrich article by ID", OptionSpec.OptionType.INTEGER),
                        OptionSpec.flag("all", "Enrich all RAW articles")
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

        List<ArticleEnricher> enrichers = parserRegistry.getAllEnrichers();
        EnrichmentService enrichmentService = new EnrichmentService(enrichers);
        List<Article> articlesToEnrich;

        if (parsedCommand.hasOption("id")) {
            articlesToEnrich = handleEnrichById(parsedCommand);
        } else {
            articlesToEnrich = handleEnrichAll();
        }

        if (articlesToEnrich.isEmpty()) {
            return;
        }

        enrichmentService.enrichAll(articlesToEnrich);

        // save
        int savedCount = 0;
        for (Article article : articlesToEnrich) {
            try {
                databaseService.saveArticle(article);
                savedCount++;
            } catch (Exception e) {
                System.err.println("Failed to save article: " + article.getUrl());
            }
        }

        System.out.println("Successfully enriched and updated " + savedCount + " articles");
    }

    private List<Article> handleEnrichById(ParsedCommand parsedCommand) {
        try {
            long id = Long.parseLong(parsedCommand.getOption("id"));
            Optional<Article> optionalArticle = databaseService.getArticleRepository().findById(id);

            if (optionalArticle.isEmpty()) {
                System.err.println("No article found with ID: " + id);
                return List.of();
            }

            return List.of(optionalArticle.get());
        } catch (NumberFormatException e) {
            System.err.println("Invalid value for --id. Must be a number.");
            return List.of();
        }
    }

    private List<Article> handleEnrichAll() {
        System.out.println("Fetching all RAW articles...");
        List<Article> articles = databaseService.getArticleRepository().findByStatus(ArticleStatus.RAW);

        if (articles.isEmpty()) {
            System.out.println("No articles found for enrichment. Run 'parse' command first.");
            return List.of();
        }

        System.out.println("Found " + articles.size() + " articles for enrichment.");
        return articles;
    }
}