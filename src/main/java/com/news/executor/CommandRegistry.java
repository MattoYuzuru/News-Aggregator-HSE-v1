package com.news.executor;

import com.news.executor.impl.*;
import com.news.storage.DatabaseService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CommandRegistry {
    private final Map<String, Command> commands = new HashMap<>();

    public CommandRegistry() {
        commands.put("help", new HelpCommand());
    }

    public void
    registerWithDatabaseService(DatabaseService databaseService) {
        // Register commands that need database access
        commands.put("parse", new ParseCommand(databaseService));
        commands.put("export", new ExportCommand(databaseService));
        commands.put("enrich", new EnrichCommand(databaseService));
        commands.put("supplement", new SupplementCommand(databaseService));

        // Add new commands for article management
        commands.put("list", new ListCommand(databaseService));
        commands.put("read", new ReadCommand(databaseService));
        commands.put("clear", new ClearCommand(databaseService));
    }

    public Optional<Command> getCommand(String name) {
        return Optional.ofNullable(commands.get(name));
    }

    public Map<String, Command> getAllCommands() {
        return new HashMap<>(commands);
    }
}
