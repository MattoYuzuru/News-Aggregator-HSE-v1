package com.news.executor;

import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

import com.news.executor.impl.*;


public class CommandRegistry {
    private final Map<String, Command> commands = new HashMap<>();

    public CommandRegistry() {
        commands.put("help", new HelpCommand());
        commands.put("export", new ExportCommand());
        commands.put("parse", new ParseCommand());
        commands.put("enrich", new EnrichCommand());
        commands.put("supplement", new SupplementCommand());

        // complete help command
        commands.put("list", new ListCommand());     // List articles with filtering options
        commands.put("read", new ReadCommand());     // Read a specific article in detail
        commands.put("save", new SaveCommand());     // Save articles to database
        commands.put("clear", new ClearCommand());   // Clear the database
        commands.put("search", new SearchCommand()); // Search through articles
    }

    public Optional<Command> getCommand(String name) {
        return Optional.ofNullable(commands.get(name));
    }

    public Set<String> getRegisteredCommands() {
        return commands.keySet();
    }
}
