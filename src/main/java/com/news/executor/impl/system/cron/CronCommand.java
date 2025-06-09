package com.news.executor.impl.system.cron;

import com.news.executor.ValidatableCommand;
import com.news.executor.impl.parsing.EnrichCommand;
import com.news.executor.impl.parsing.ParseCommand;
import com.news.executor.impl.parsing.SupplementCommand;
import com.news.executor.spec.CommandSpec;
import com.news.executor.spec.OptionSpec;
import com.news.model.ParsedCommand;
import com.news.parser.ParserRegistry;
import com.news.storage.DatabaseService;

import java.io.Console;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.news.ConfigLoader.AI_SUPPLEMENT_TIME_PER_ARTICLE;

public class CronCommand implements ValidatableCommand {

    private final DatabaseService databaseService;
    private final ParserRegistry parserRegistry;
    private final CommandSpec commandSpec;
    private final CronScheduler cronScheduler;

    public CronCommand(DatabaseService databaseService, ParserRegistry parserRegistry) {
        this.databaseService = databaseService;
        this.parserRegistry = parserRegistry;
        this.cronScheduler = new CronScheduler();
        this.commandSpec = createCommandSpec();
        setupShutdownHook();
    }

    private CommandSpec createCommandSpec() {
        return new CommandSpec.Builder()
                .name("cron")
                .description("Set auto-parsing with scheduled intervals")
                .options(Set.of(
                        OptionSpec.flag("web", "Only web parsing without AI enrichment"),
                        OptionSpec.flag("full", "Full parsing with AI enrichment (Takes long time!)"),
                        OptionSpec.withMultipleArgs("source", "Source names or 'all' for all sources", 1, Integer.MAX_VALUE, OptionSpec.OptionType.STRING),
                        OptionSpec.withMultipleArgs("time", "Set parsing schedule (format: hour minute second period_in_seconds)", 4, 4, OptionSpec.OptionType.STRING),
                        OptionSpec.withSingleArg("limit", "Limit number of articles per source", OptionSpec.OptionType.INTEGER),
                        OptionSpec.flag("stop", "Stop the currently running cron job")
                ))
                .requiredOptions(Set.of())
                .mutuallyExclusiveGroups(Set.of(Set.of("web", "full")))
                .build();
    }

    private void setupShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nReceived termination signal. Stopping cron job gracefully...");
            cronScheduler.stop();
        }));
    }

    @Override
    public CommandSpec getCommandSpec() {
        return commandSpec;
    }

    @Override
    public void executeValidated(ParsedCommand parsedCommand) {
        if (parsedCommand.hasOption("stop")) {
            cronScheduler.stop();
            return;
        }

        if (!parsedCommand.hasOption("time")) {
            printUsage();
            return;
        }

        try {
            CronSchedule schedule = parseCronSchedule(parsedCommand.getOptionValues("time"));

            boolean isFullMode = parsedCommand.hasOption("full");
            List<String> sources = parsedCommand.getOptionValues("source");
            Integer limit = parsedCommand.hasOption("limit") ?
                    Integer.parseInt(parsedCommand.getOptionValues("limit").getFirst()) : null;

            if (isFullMode && !validateFullModeExecution(schedule, limit)) {
                System.out.println("❌ Full mode execution cancelled");
                return;
            }

            startCronJob(schedule, isFullMode, sources, limit);

        } catch (Exception e) {
            System.err.println("Error setting up cron job: " + e.getMessage());
        }
    }

    private void printUsage() {
        System.out.println("Error: --time option is required for cron scheduling");
        System.out.println("Usage: cron --time <hour> <minute> <second> <period_in_seconds>");
        System.out.println("Example: cron --time 2 0 0 3600 (every hour starting at 2:00 AM)");
        System.out.println("Example: cron --time 14 30 0 7200 (every 2 hours starting at 2:30 PM)");
    }

    private CronSchedule parseCronSchedule(List<String> timeArgs) {
        try {
            int hour = Integer.parseInt(timeArgs.get(0));
            int minute = Integer.parseInt(timeArgs.get(1));
            int second = Integer.parseInt(timeArgs.get(2));
            long periodSeconds = Long.parseLong(timeArgs.get(3));

            validateTimeParameters(hour, minute, second, periodSeconds);

            return CronSchedule.of(hour, minute, second, periodSeconds);
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Invalid time format. Expected: <hour> <minute> <second> <period_in_seconds>");
        }
    }

    private void validateTimeParameters(int hour, int minute, int second, long periodSeconds) {
        if (hour < 0 || hour > 23) {
            throw new IllegalArgumentException("Hour must be between 0 and 23");
        }
        if (minute < 0 || minute > 59) {
            throw new IllegalArgumentException("Minute must be between 0 and 59");
        }
        if (second < 0 || second > 59) {
            throw new IllegalArgumentException("Second must be between 0 and 59");
        }
        if (periodSeconds <= 0) {
            throw new IllegalArgumentException("Period must be greater than 0 seconds");
        }
    }

    private boolean validateFullModeExecution(CronSchedule schedule, Integer limit) {
        try {
            int estimatedArticles = estimateArticleCount(limit);
            long estimatedTimeMinutes = (long) estimatedArticles * AI_SUPPLEMENT_TIME_PER_ARTICLE;
            long periodMinutes = schedule.getPeriodMinutes();

            System.out.println("⚠️  WARNING: AI supplement mode enabled!");
            System.out.printf("   Estimated articles to process: %d%n", estimatedArticles);
            System.out.printf("   Estimated processing time: %d minutes (%d hours)%n",
                    estimatedTimeMinutes, estimatedTimeMinutes / 60);
            System.out.printf("   Cron period: %d minutes%n", periodMinutes);

            if (estimatedTimeMinutes > periodMinutes) {
                System.err.printf("❌ ERROR: Estimated processing time (%d min) exceeds cron period (%d min)!%n",
                        estimatedTimeMinutes, periodMinutes);
                System.err.println("   This will cause overlapping executions. Please:");
                System.err.println("   1. Increase the cron period, or");
                System.err.println("   2. Use --limit to reduce articles per run, or");
                System.err.println("   3. Use --web flag instead of --full");
                return false;
            }

            System.out.print("Continue with AI supplement? (y/N): ");
            Console console = System.console();
            if (console != null) {
                String input = console.readLine().trim().toLowerCase();
                return "y".equals(input);
            } else {
                // Fallback for when console is not available
                System.err.println("Warning: Console not available, defaulting to 'no'");
                return false;
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not get user input: " + e.getMessage());
            return false;
        }

    }

    private int estimateArticleCount(Integer limit) {
        try {
            int totalArticles = databaseService.getArticleRepository().findAll().size();
            return limit != null ? Math.min(limit, totalArticles) : totalArticles;
        } catch (Exception e) {
            return limit != null ? limit : 100;
        }
    }

    private void startCronJob(CronSchedule schedule, boolean isFullMode, List<String> sources, Integer limit) {
        if (cronScheduler.isRunning()) {
            System.out.println("ℹ️  Stopping existing cron job...");
            cronScheduler.stop();
        }

        String mode = isFullMode ? "Full (with AI)" : "Web only";
        String sourcesDesc = sources != null ? String.join(", ", sources) : "all";

        Runnable cronTask = () -> executeParsingSequence(isFullMode, sources, limit);

        cronScheduler.schedule(schedule, cronTask, mode, sourcesDesc);
    }

    private void executeParsingSequence(boolean isFullMode, List<String> sources, Integer limit) {
        try {
            if (cronScheduler.shouldStop()) return;

            System.out.println("  📥 Executing: parse --source all");
            executeParsing(sources, limit);

            if (cronScheduler.shouldStop()) return;

            System.out.println("  🔍 Executing: enrich --all");
            executeEnriching();

            if (cronScheduler.shouldStop()) return;

            if (isFullMode) {
                System.out.println("  🤖 Executing: supplement --all (AI processing...)");
                executeSupplementing();
            }

        } catch (Exception e) {
            throw new RuntimeException("Error in parsing sequence", e);
        }
    }

    private void executeParsing(List<String> sources, Integer limit) {
        List<String> sourceList = sources != null ? sources : List.of("all");
        ParsedCommand parsedCommand = ParsedCommand.builder()
                .name("parse")
                .options(Map.of("source", sourceList))
                .build();

        ParseCommand parseCommand = new ParseCommand(databaseService, parserRegistry);
        parseCommand.executeValidated(parsedCommand);
    }

    private void executeEnriching() {
        ParsedCommand parsedCommand = ParsedCommand.builder()
                .name("enrich")
                .options(Map.of("all", List.of("true")))
                .build();
        EnrichCommand enrichCommand = new EnrichCommand(databaseService, parserRegistry);
        enrichCommand.executeValidated(parsedCommand);
    }

    private void executeSupplementing() {
        ParsedCommand parsedCommand = ParsedCommand.builder()
                .name("supplement")
                .options(Map.of("all", List.of("true")))
                .build();
        SupplementCommand supplementCommand = new SupplementCommand(databaseService);
        supplementCommand.executeValidated(parsedCommand);
    }
}