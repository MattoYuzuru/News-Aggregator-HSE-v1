package com.news.executor;

import com.news.executor.impl.parsing.EnrichCommand;
import com.news.executor.impl.parsing.ParseCommand;
import com.news.executor.impl.parsing.SupplementCommand;
import com.news.executor.impl.system.cron.CronCommand;
import com.news.executor.impl.system.ExitCommand;
import com.news.executor.impl.system.export.ExportCommand;
import com.news.executor.impl.system.HelpCommand;
import com.news.executor.impl.manip.*;
import com.news.parser.ParserRegistry;
import com.news.storage.DatabaseService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class CommandRegistry {
    private final Map<String, Command> commands = new HashMap<>();

    public CommandRegistry() {
        commands.put("help", new HelpCommand(this));
    }

    public void registerWithDatabaseService(DatabaseService databaseService) {
        commands.put("export", new ExportCommand(databaseService));
        commands.put("supplement", new SupplementCommand(databaseService));
        commands.put("list", new ListCommand(databaseService));
        commands.put("read", new ReadCommand(databaseService));
        commands.put("search", new SearchCommand(databaseService));
        commands.put("clear", new ClearCommand(databaseService));
        commands.put("stats", new StatsCommand(databaseService));
        commands.put("exit", new ExitCommand(databaseService));
    }

    public void registerWithDatabaseServiceAndParserRegister(DatabaseService databaseService, ParserRegistry parserRegistry) {
        commands.put("parse", new ParseCommand(databaseService, parserRegistry));
        commands.put("enrich", new EnrichCommand(databaseService, parserRegistry));
        commands.put("cron", new CronCommand(databaseService, parserRegistry));
    }

    public Optional<Command> getCommand(String name) {
        return Optional.ofNullable(commands.get(name));
    }

    public Set<String> getAvailableCommands() {
        return commands.keySet();
    }
}
