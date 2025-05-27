package com.news.executor;

import com.news.executor.impl.*;
import com.news.parser.ParserRegistry;
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
        commands.put("export", new ExportCommand(databaseService));
        commands.put("supplement", new SupplementCommand(databaseService));
        commands.put("list", new ListCommand(databaseService));
        commands.put("read", new ReadCommand(databaseService));
        commands.put("search", new SearchCommand(databaseService));
        commands.put("clear", new ClearCommand(databaseService));
        commands.put("stats", new StatsCommand(databaseService));
        commands.put("exit", new ExitCommand(databaseService));
    }
    public void
    registerWithDatabaseServiceAndParserRegister(DatabaseService databaseService, ParserRegistry parserRegistry) {
        // Register commands that need database access and parser registry
        commands.put("parse", new ParseCommand(databaseService, parserRegistry));
        commands.put("enrich", new EnrichCommand(databaseService, parserRegistry));
    }

    public Optional<Command> getCommand(String name) {
        return Optional.ofNullable(commands.get(name));
    }
}
