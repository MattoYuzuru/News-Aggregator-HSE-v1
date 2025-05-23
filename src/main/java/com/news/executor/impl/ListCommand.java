package com.news.executor.impl;

import com.news.executor.Command;
import com.news.model.ParsedCommand;
import com.news.storage.DatabaseService;

public class ListCommand implements Command {
    public ListCommand(DatabaseService databaseService) {
    }

    @Override
    public void execute(ParsedCommand parsedCommand) {

    }
}
