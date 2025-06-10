package com.news.executor.impl.parsing;

import com.news.ai.AIAnalysisService;
import com.news.ai.ArticleAnalyzer;
import com.news.ai.ConfigurableArticleAnalyzer;
import com.news.ai.config.AIConfiguration;
import com.news.executor.ValidatableCommand;
import com.news.executor.spec.CommandSpec;
import com.news.executor.spec.OptionSpec;
import com.news.model.Article;
import com.news.model.ArticleStatus;
import com.news.model.ParsedCommand;
import com.news.storage.DatabaseService;

import java.util.*;

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
                        OptionSpec.withSingleArg("model", "AI model to use (default: qwen3:4b)", OptionSpec.OptionType.STRING),
                        OptionSpec.flag("all", "Supplement all ENRICHED articles"),
                        OptionSpec.flag("summarize", "Enable summarization"),
                        OptionSpec.flag("classify-region", "Enable region classification"),
                        OptionSpec.flag("generate-tags", "Enable tag generation"),
                        OptionSpec.flag("evaluate", "Enable article evaluation")
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

        List<Article> articlesToSupplement;

        if (parsedCommand.hasOption("id")) {
            articlesToSupplement = handleSupplementById(parsedCommand);
        } else {
            articlesToSupplement = handleSupplementAll();
        }

        if (articlesToSupplement.isEmpty()) {
            return;
        }

        // Create AI configuration based on command line options
        AIConfiguration config = createAIConfiguration(parsedCommand);
        ArticleAnalyzer articleAnalyzer = new ConfigurableArticleAnalyzer(config);
        AIAnalysisService analysisService = new AIAnalysisService(databaseService, articleAnalyzer);

        int savedCount = analysisService.analyzeArticles(articlesToSupplement);
        System.out.println("Successfully supplemented and updated " + savedCount + " articles using model: " + config.modelName());
        System.out.println("Enabled operations: " + config.enabledOperations());
    }

    private AIConfiguration createAIConfiguration(ParsedCommand parsedCommand) {
        // Default model
        String model = parsedCommand.getOption("model");
        if (model == null || model.isEmpty()) {
            model = "qwen3:4b";
        }

        // Determine enabled operations
        Set<String> enabledOperations = new HashSet<>();

        // If no specific operations are specified, enable all by default
        boolean hasSpecificOperations =
                parsedCommand.hasOption("summarize") ||
                parsedCommand.hasOption("classify-region") ||
                parsedCommand.hasOption("generate-tags") ||
                parsedCommand.hasOption("evaluate");

        if (!hasSpecificOperations) {
            enabledOperations.addAll(Set.of("summarization", "region_classification", "tag_generation", "evaluation"));
        } else {
            if (parsedCommand.hasOption("summarize")) {
                enabledOperations.add("summarization");
            }
            if (parsedCommand.hasOption("classify-region")) {
                enabledOperations.add("region_classification");
            }
            if (parsedCommand.hasOption("generate-tags")) {
                enabledOperations.add("tag_generation");
            }
            if (parsedCommand.hasOption("evaluate")) {
                enabledOperations.add("evaluation");
            }
        }

        return new AIConfiguration(model, enabledOperations);
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
            if (!article.getStatus().equals(ArticleStatus.ENRICHED) && !article.getStatus().equals(ArticleStatus.ANALYZED)) {
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