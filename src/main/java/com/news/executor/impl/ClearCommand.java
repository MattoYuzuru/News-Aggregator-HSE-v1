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
        if (parsedCommand.hasOption("delete")) {
            try {
                databaseService.cleanupDatabase();
            } catch (SQLException e) {
                System.out.println("Error while cleaning up");
            }
        }
    }
}
