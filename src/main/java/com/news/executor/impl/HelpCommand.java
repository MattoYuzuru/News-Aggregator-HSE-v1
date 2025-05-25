package com.news.executor.impl;

import com.news.executor.Command;
import com.news.model.ParsedCommand;

public class HelpCommand implements Command {

    @Override
    public void execute(ParsedCommand parsedCommand) {
        String command = parsedCommand.getOptionValues("command") != null &&
                !parsedCommand.getOptionValues("command").isEmpty()
                ? parsedCommand.getOptionValues("command").getFirst()
                : null;

        if (command == null) {
            printGeneralHelp();
        } else {
            switch (command.toLowerCase()) {
                case "parse" -> printParseHelp();
                case "export" -> printExportHelp();
                case "enrich" -> printEnrichHelp();
                case "supplement" -> printSupplementHelp();
                case "list" -> printListHelp();
                case "read" -> printReadHelp();
                case "clear" -> printClearHelp();
                case "search" -> printSearchHelp();
                case "exit" -> printExitHelp();
                default -> System.out.println("Unknown command: " + command);
            }
        }
    }

    private void printGeneralHelp() {
        System.out.println("News Processing Tool - Available Commands");
        System.out.println("=========================================");
        System.out.println();
        System.out.println("COMMANDS:");
        System.out.println("  help        Display help information for all commands");
        System.out.println("              Usage: help [--command COMMAND_NAME]");
        System.out.println();
        System.out.println("  parse       Fetch news articles from specified sources");
        System.out.println("              Usage: parse --source SOURCE1 [SOURCE2...] [--limit NUMBER]");
        System.out.println();
        System.out.println("  export      Export articles to various formats");
        System.out.println("              Usage: export --format FORMAT [--output FILEPATH]");
        System.out.println();
        System.out.println("  enrich      Enhance articles with additional metadata");
        System.out.println("              Usage: enrich [options]");
        System.out.println();
        System.out.println("  supplement  Add supplementary information to articles");
        System.out.println("              Usage: supplement [options]");
        System.out.println();
        System.out.println("  list        Display a list of articles' ids, titles and published dates");
        System.out.println("              Usage: list [options]");
        System.out.println();
        System.out.println("  read        Show the contents of the article with the provided id");
        System.out.println("              Usage: read --id ARTICLE_ID");
        System.out.println();
        System.out.println("  save        Save current state or changes");
        System.out.println("              Usage: save [options]");
        System.out.println();
        System.out.println("  clear       Clear specified entities or reset states");
        System.out.println("              Usage: clear [options]");
        System.out.println();
        System.out.println("  exit        Close the program");
        System.out.println();
        System.out.println("For more details on a specific command, run: help --command COMMAND_NAME");
    }

    private void printParseHelp() {
        System.out.println("COMMAND: parse");
        System.out.println("==============");
        System.out.println("Fetch news articles from specified sources");
        System.out.println();
        System.out.println("OPTIONS:");
        System.out.println("  --source SOURCE1 [SOURCE2...]   Specify one or more news sources to parse");
        System.out.println("                                  Use 'all' to fetch from all available sources");
        System.out.println("                                  Available sources: nhk, bbc, etc.");
        System.out.println("  --limit NUMBER                  Maximum number of articles to fetch per source");
        System.out.println();
        System.out.println("EXAMPLES:");
        System.out.println("  parse --source nhk");
        System.out.println("  parse --source nhk bbc");
        System.out.println("  parse --source all --limit 10");
    }

    private void printExportHelp() {
        System.out.println("COMMAND: export");
        System.out.println("===============");
        System.out.println("Export articles to various formats");
        System.out.println();
        System.out.println("OPTIONS:");
        System.out.println("  --format FORMAT     Specify output format (CSV, JSON, HTML)");
        System.out.println("  --output FILEPATH   Path to save the exported file (optional)");
        System.out.println();
        System.out.println("EXAMPLES:");
        System.out.println("  export --format CSV");
        System.out.println("  export --format JSON --output data.json");
    }

    private void printEnrichHelp() {
        System.out.println("COMMAND: enrich");
        System.out.println("===============");
        System.out.println("Enhance articles with additional metadata like categories, article content, region, authors, etc.");
        System.out.println();
        System.out.println("EXAMPLES:");
        System.out.println("  enrich");
    }

    private void printSupplementHelp() {
        System.out.println("COMMAND: supplement");
        System.out.println("====================");
        System.out.println("Add supplementary information using AI, such as summary, tags, region, etc.");
        System.out.println();
        System.out.println("EXAMPLES:");
        System.out.println("  supplement");
    }

    private void printListHelp() {
        System.out.println("COMMAND: list");
        System.out.println("==============");
        System.out.println("Display articles from the database using filters");
        System.out.println();
        System.out.println("EXAMPLES:");
        System.out.println("  list");
        System.out.println("  list --status ENRICHED");
        System.out.println("  list --limit 12");
        System.out.println("  list --limit 12 --offset 5");
        System.out.println("  list --source NHK");
        System.out.println("  list --lang ENG");
        System.out.println("  list --author \"John Doe\"");
        System.out.println("  list --today");
        System.out.println("  list --tag \"Cute cat\"");
        System.out.println("  list --tag cat dog"); // finds tags cat and dog
        System.out.println("  list --published 2025-05-23");
        System.out.println("  list --limit 5 --lang ENG --source BBC --published 2025-05-23");
    }

    private void printReadHelp() {
        System.out.println("COMMAND: read");
        System.out.println("==============");
        System.out.println("Show full content of the article with the given ID");
        System.out.println();
        System.out.println("EXAMPLES:");
        System.out.println("  read --id 123");
        System.out.println("  read --id 123 --no-content");
    }

    private void printClearHelp() {
        System.out.println("COMMAND: clear");
        System.out.println("===============");
        System.out.println("Delete article with given ID / articles from the DB");
        System.out.println();
        System.out.println("EXAMPLES:");
        System.out.println("  clear --id");
        System.out.println("  clear --all");
    }

    private void printSearchHelp() {
        System.out.println("COMMAND: search");
        System.out.println("===============");
        System.out.println("Find articles using parts of their title of content");
        System.out.println();
        System.out.println("EXAMPLES:");
        System.out.println("  search --title \"Meow meow meow\"");
        System.out.println("  search --content \"Lorem ipsum...\"");
    }

    private void printExitHelp() {
        System.out.println("COMMAND: exit");
        System.out.println("===============");
        System.out.println("Exit from the program with or without saving progress");
        System.out.println();
        System.out.println("EXAMPLES:");
        System.out.println("  exit");
        System.out.println("  exit --delete");
    }
}