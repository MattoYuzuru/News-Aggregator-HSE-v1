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
        if (parsedCommand.hasOption("id")) {
            Integer id = Integer.parseInt(parsedCommand.getOption("id"));
            databaseService.getArticleRepository().deleteById(id);
        }
    }
}
