package com.news.executor.impl;

import com.news.executor.Command;
import com.news.model.ParsedCommand;

public class HelpCommand implements Command {

    @Override
    public void execute(ParsedCommand parsedCommand) {
        String command = parsedCommand.getOptionValues("command") != null &&
                !parsedCommand.getOptionValues("command").isEmpty() ?
                parsedCommand.getOptionValues("command").getFirst() : null;

        if (command == null) {
            printGeneralHelp();
        } else {
            switch (command.toLowerCase()) {
                case "parse" -> printParseHelp();
                case "export" -> printExportHelp();
                case "enrich" -> printEnrichHelp();
                case "supplement" -> printSupplementHelp();
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
        System.out.println("             Usage: help [--command COMMAND_NAME]");
        System.out.println();
        System.out.println("  parse       Fetch news articles from specified sources");
        System.out.println("             Usage: parse --source SOURCE1 [SOURCE2...] [--limit NUMBER]");
        System.out.println();
        System.out.println("  export      Export articles to various formats");
        System.out.println("             Usage: export --format FORMAT [--output FILEPATH]");
        System.out.println();
        System.out.println("  enrich      Enhance articles with additional metadata");
        System.out.println("             Usage: enrich [options]");
        System.out.println();
        System.out.println("  supplement  Add supplementary information to articles");
        System.out.println("             Usage: supplement [options]");
        System.out.println();
        System.out.println("For more details on a specific command, run: help --command COMMAND_NAME");
    }

    private void printParseHelp() {
        System.out.println("COMMAND: parse");
        System.out.println("=============");
        System.out.println("Fetch news articles from specified sources");
        System.out.println();
        System.out.println("OPTIONS:");
        System.out.println("  --source SOURCE1 [SOURCE2...]   Specify one or more news sources to parse");
        System.out.println("                                 Use 'all' to fetch from all available sources");
        System.out.println("                                 Available sources: nhk, bbc, etc.");
        System.out.println();
        System.out.println("  --limit NUMBER                 Maximum number of articles to fetch per source");
        System.out.println();
        System.out.println("EXAMPLES:");
        System.out.println("  parse --source nhk             Parse articles from NHK");
        System.out.println("  parse --source nhk bbc         Parse articles from NHK and BBC");
        System.out.println("  parse --source all --limit 10  Parse up to 10 articles from each available source");
    }

    private void printExportHelp() {
        System.out.println("COMMAND: export");
        System.out.println("==============");
        System.out.println("Export articles to various formats");
        System.out.println();
        System.out.println("OPTIONS:");
        System.out.println("  --format FORMAT     Specify output format (CSV, JSON, HTML)");
        System.out.println("  --output FILEPATH   Path to save the exported file (optional)");
        System.out.println();
        System.out.println("EXAMPLES:");
        System.out.println("  export --format CSV                Export articles to CSV format");
        System.out.println("  export --format JSON --output data.json   Export to JSON file");
    }

    private void printEnrichHelp() {
        System.out.println("COMMAND: enrich");
        System.out.println("==============");
        System.out.println("Enhance articles with additional metadata like categories, entity extraction, etc.");
        System.out.println();
        System.out.println("OPTIONS:");
        System.out.println("  Use 'help --command enrich' to see available options");
        System.out.println();
        System.out.println("EXAMPLES:");
        System.out.println("  enrich               Enrich all articles with default settings");
    }

    private void printSupplementHelp() {
        System.out.println("COMMAND: supplement");
        System.out.println("=================");
        System.out.println("Add supplementary information to articles, such as related content");
        System.out.println();
        System.out.println("OPTIONS:");
        System.out.println("  Use 'help --command supplement' to see available options");
        System.out.println();
        System.out.println("EXAMPLES:");
        System.out.println("  supplement          Apply default supplementary information to articles");
    }
}