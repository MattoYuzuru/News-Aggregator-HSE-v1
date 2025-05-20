package com.news.executor;

import com.news.model.ParsedCommand;

public interface Command {
    void execute(ParsedCommand parsedCommand);
}
