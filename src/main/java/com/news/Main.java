package com.news;

import com.news.executor.CliEngine;
import com.news.executor.CommandExecutorService;
import com.news.executor.CommandRegistry;
import com.news.storage.DatabaseService;

public class Main {
    public static void main(String[] args) {
        try {
            // init db service
            DatabaseService databaseService = new DatabaseService();

            // reg commands with db access
            CommandRegistry registry = new CommandRegistry();
            registry.registerWithDatabaseService(databaseService);

            CommandExecutorService executor = new CommandExecutorService(registry);
            CliEngine engine = new CliEngine(executor);

            // Add shutdown hook to close database connection | gpt fix
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Closing database connections...");
                databaseService.close();
            }));

            // run cli engine
            engine.run();

        } catch (Exception e) {
            System.err.println("Failed to init application: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}   