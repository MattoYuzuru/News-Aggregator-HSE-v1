package com.news.executor.impl;

import com.news.executor.Command;
import com.news.model.ParsedCommand;
import com.news.storage.DatabaseService;

import java.sql.SQLException;

public class ClearCommand implements Command {
    private final DatabaseService databaseService;

    public ClearCommand(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @Override
    public void execute(ParsedCommand parsedCommand) {
        boolean hasId = parsedCommand.hasOption("id");
        boolean hasAll = parsedCommand.hasOption("all");

        if (hasId && hasAll) {
            System.out.println("Error: Cannot use both --id and --all.");
        } else if (!hasId && !hasAll) {
            System.out.println("Error: Must use either --id or --all.");
        } else if (hasId) {
            Long id = Long.parseLong(parsedCommand.getOption("id"));
            databaseService.getArticleRepository().deleteById(id);
            System.out.println("Successfully deleted article with id: " + id);
        } else {
            try {
                databaseService.cleanupDatabase();
                System.out.println("Successfully cleaned up the database.");
            } catch (SQLException e) {
                System.out.println("Error while cleaning up the database: " + e.getMessage());
            }
        }
    }
}
