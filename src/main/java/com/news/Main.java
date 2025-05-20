package com.news;

import com.news.executor.CliEngine;
import com.news.executor.CommandExecutorService;
import com.news.executor.CommandRegistry;

public class Main {
    public static void main(String[] args) {
        CommandRegistry registry = new CommandRegistry();
        CommandExecutorService executor = new CommandExecutorService(registry);
        CliEngine engine = new CliEngine(executor);
        engine.run();
    }
}
