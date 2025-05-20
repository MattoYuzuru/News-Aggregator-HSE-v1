package com.news.executor;

import java.util.Scanner;

public class CliEngine {
    private final Scanner scanner = new Scanner(System.in);
    private final CommandExecutorService executor;

    public CliEngine(CommandExecutorService executor) {
        this.executor = executor;
    }

    public void run() {
        System.out.println("News Aggregator CLI. Type 'help' for commands, 'exit' to quit.");

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("exit") || input.equalsIgnoreCase("quit")) {
                System.out.println("Goodbye!");
                break;
            }

            executor.execute(input);
        }
    }
}
