package com.news.executor.impl;

import com.news.executor.Command;
import com.news.export.ExportService;
import com.news.export.FileService;
import com.news.model.ExportFormat;
import com.news.model.ParsedCommand;
import com.news.storage.DatabaseService;

import java.io.File;
import java.io.IOException;

public class ExportCommand implements Command {
    private final DatabaseService databaseService;

    public ExportCommand(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @Override
    public void execute(ParsedCommand parsedCommand) {
        if (!parsedCommand.hasOption("format")) {
            System.err.println("Error: Missing required option 'format'.");
            return;
        }

        ExportFormat format;
        try {
            format = ExportFormat.valueOf(parsedCommand.getOption("format").toUpperCase());
        } catch (IllegalArgumentException e) {
            System.err.println("Error: Invalid format specified.");
            return;
        }

        String dirPath = parsedCommand.getOptionOrDefault("dir", "exports");
        File outputDir;
        FileService fileService = new FileService();
        try {
            outputDir = fileService.createDirectoryIfNotExists(dirPath);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return;
        }

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
        } catch (NumberFormatException e) {
            System.err.println("Error: Invalid article ID.");
            return;
        } catch (Exception e) {
            System.err.println("Error during export: " + e.getMessage());
            return;
        }

        File outputFile = new File(outputDir, fileName);
        try {
            fileService.writeToFile(outputFile, exportedContent);
            System.out.println("Export successful! File saved to: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }
}