package com.news.executor.impl;

import com.news.executor.ValidatableCommand;
import com.news.executor.spec.CommandSpec;
import com.news.executor.spec.OptionSpec;
import com.news.export.ExportService;
import com.news.export.FileService;
import com.news.model.ExportFormat;
import com.news.model.ParsedCommand;
import com.news.storage.DatabaseService;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class ExportCommand implements ValidatableCommand {
    private final DatabaseService databaseService;
    private final CommandSpec commandSpec;

    public ExportCommand(DatabaseService databaseService) {
        this.databaseService = databaseService;
        this.commandSpec = new CommandSpec.Builder()
                .name("export")
                .description("Export articles to file")
                .options(Set.of(
                        OptionSpec.withSingleArg("format", "Export format (JSON, XML, CSV)", OptionSpec.OptionType.STRING),
                        OptionSpec.withSingleArg("dir", "Output directory (default: exports)", OptionSpec.OptionType.STRING),
                        OptionSpec.withSingleArg("id", "Export specific article by ID", OptionSpec.OptionType.INTEGER)
                ))
                .requiredOptions(Set.of("format"))
                .build();
    }

    @Override
    public CommandSpec getCommandSpec() {
        return commandSpec;
    }

    @Override
    public void executeValidated(ParsedCommand parsedCommand) {
        // Validate format
        ExportFormat format;
        try {
            format = ExportFormat.valueOf(parsedCommand.getOption("format").toUpperCase());
        } catch (IllegalArgumentException e) {
            System.err.println("Error: Invalid format '" + parsedCommand.getOption("format") +
                    "'. Valid formats: " + String.join(", ", getValidFormats()));
            return;
        }

        // Handle directory
        String dirPath = parsedCommand.getOptionOrDefault("dir", "exports");
        File outputDir;
        FileService fileService = new FileService();
        try {
            outputDir = fileService.createDirectoryIfNotExists(dirPath);
        } catch (IOException e) {
            System.err.println("Error creating directory: " + e.getMessage());
            return;
        }

        // Export logic
        ExportService exportService = new ExportService(databaseService.getArticleRepository());
        String exportedContent;
        String fileName;

        try {
            if (parsedCommand.hasOption("id")) {
                Long id = Long.parseLong(parsedCommand.getOption("id"));
                exportedContent = exportService.exportById(id, format);
                fileName = "article_" + id + "." + format.name().toLowerCase();
            } else {
                exportedContent = exportService.exportAll(format);
                fileName = "articles_all." + format.name().toLowerCase();
            }
        } catch (Exception e) {
            System.err.println("Error during export: " + e.getMessage());
            return;
        }

        // Write to file
        File outputFile = new File(outputDir, fileName);
        try {
            fileService.writeToFile(outputFile, exportedContent);
            System.out.println("Export successful! File saved to: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }

    private String[] getValidFormats() {
        return java.util.Arrays.stream(ExportFormat.values())
                .map(Enum::name)
                .toArray(String[]::new);
    }
}