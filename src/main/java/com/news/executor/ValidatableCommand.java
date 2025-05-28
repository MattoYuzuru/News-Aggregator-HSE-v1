package com.news.executor;

import com.news.executor.spec.CommandSpec;
import com.news.model.ParsedCommand;

public interface ValidatableCommand extends Command {
    CommandSpec getCommandSpec();

    @Override
    default void execute(ParsedCommand parsedCommand) {
        // delegate to executeValidated for ValidatableCommands
        // only call for legacy, new cmds don't need!!
        executeValidated(parsedCommand);
    }

    void executeValidated(ParsedCommand parsedCommand);
}