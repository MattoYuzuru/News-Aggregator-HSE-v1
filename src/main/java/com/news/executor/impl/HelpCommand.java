package com.news.executor.impl;

import com.news.executor.Command;
import com.news.executor.CommandRegistry;
import com.news.executor.ValidatableCommand;
import com.news.executor.spec.CommandSpec;
import com.news.executor.spec.OptionSpec;
import com.news.model.ParsedCommand;

import java.util.Set;

public class HelpCommand implements ValidatableCommand {
    private final CommandSpec commandSpec;
    private final CommandRegistry commandRegistry;

    public HelpCommand(CommandRegistry commandRegistry) {
        this.commandRegistry = commandRegistry;
        this.commandSpec = new CommandSpec.Builder()
                .name("help")
                .description("Display help information")
                .options(Set.of(
                        OptionSpec.withSingleArg("command", "Show help for specific command", OptionSpec.OptionType.STRING)
                ))
                .build();
    }

    @Override
    public CommandSpec getCommandSpec() {
        return commandSpec;
    }

    @Override
    public void executeValidated(ParsedCommand parsedCommand) {
        if (parsedCommand.hasOption("command")) {
            showCommandHelp(parsedCommand.getOption("command"));
        } else {
            showGeneralHelp();
        }
    }

    private void showCommandHelp(String commandName) {
        var commandOpt = commandRegistry.getCommand(commandName);

        if (commandOpt.isEmpty()) {
            System.err.println("Unknown command: " + commandName);
            System.err.println("Available commands: " + String.join(", ", commandRegistry.getAvailableCommands()));
            return;
        }

        Command command = commandOpt.get();
        if (command instanceof ValidatableCommand validatableCommand) {
            CommandSpec spec = validatableCommand.getCommandSpec();
            System.out.println("=== " + spec.getName().toUpperCase() + " COMMAND ===");
            System.out.println(spec.getDescription());
            System.out.println();

            if (!spec.getOptions().isEmpty()) {
                System.out.println("Options:");
                for (var option : spec.getOptions()) {
                    String optionStr = "  --" + option.getName();
                    if (option.requiresArgument()) {
                        if (option.getMaxArgs() == 1) {
                            optionStr += " <value>";
                        } else {
                            optionStr += " <value1> [value2] ...";
                        }
                    }
                    System.out.printf("%-25s %s%n", optionStr, option.getDescription());
                }
                System.out.println();
            }

            if (!spec.getRequiredOptions().isEmpty()) {
                System.out.println("Required options: " +
                        String.join(", ", spec.getRequiredOptions().stream().map(o -> "--" + o).toList()));
                System.out.println();
            }

            if (!spec.getMutuallyExclusiveGroups().isEmpty()) {
                System.out.println("Mutually exclusive groups:");
                for (Set<String> group : spec.getMutuallyExclusiveGroups()) {
                    System.out.println("  " + String.join(" | ", group.stream().map(o -> "--" + o).toList()));
                }
                System.out.println();
            }

            // Show examples if available
            showExamples(commandName);
        } else {
            System.out.println("Help for legacy command '" + commandName + "' - no detailed help available.");
        }
    }

    private void showExamples(String commandName) {
        System.out.println("Examples:");
        switch (commandName) {
            case "parse" -> {
                System.out.println("  parse --source bbc");
                System.out.println("  parse --source all --limit 50");
                System.out.println("  parse --source reuters --source cnn");
            }
            case "list" -> {
                System.out.println("  list");
                System.out.println("  list --status ENRICHED --limit 5");
                System.out.println("  list --author \"John Doe\" --today");
            }
            case "search" -> {
                System.out.println("  search --title \"climate change\"");
                System.out.println("  search --content \"artificial intelligence\"");
            }
            case "export" -> {
                System.out.println("  export --format JSON");
                System.out.println("  export --format CSV --dir ./my-exports");
                System.out.println("  export --format XML --id 123");
            }
            case "clear" -> {
                System.out.println("  clear --id 123");
                System.out.println("  clear --all");
            }
            case "read" -> {
                System.out.println("  read --id 123");
                System.out.println("  read --id 123 --no-content");
            }
            default -> System.out.println("  " + commandName + " [options]");
        }
    }

    private void showGeneralHelp() {
        System.out.println("""
            ╔══════════════════════════════════════════════════════════════╗
            ║                    NEWS MANAGER HELP                         ║
            ╚══════════════════════════════════════════════════════════════╝
           \s
            Available commands:
           \s
            📰 CONTENT MANAGEMENT:
              parse      - Parse articles from news sources
              enrich     - Enrich articles with additional data \s
              supplement - Add AI-generated supplements to articles
             \s
            🔍 DISCOVERY & SEARCH:
              list       - List articles with filtering options
              search     - Search articles by content or title
              read       - Read a specific article by ID
             \s
            📊 DATA MANAGEMENT:
              export     - Export articles to various formats
              clear      - Remove articles from database
              stats      - Display database statistics
             \s
            ⚙️  SYSTEM:
              help       - Display this help (you're here!)
              exit       - Exit the application
           \s
            💡 TIPS:
              • Use 'help --command <name>' for detailed command help
              • Most commands support --help for quick reference
              • Use 'q' or 'quit' as shortcuts for 'exit'
           \s
            Examples:
              parse --source bbc --limit 10
              list --status ENRICHED --limit 5
              search --title "climate change"
              export --format JSON --dir ./exports
           \s""");
    }
}