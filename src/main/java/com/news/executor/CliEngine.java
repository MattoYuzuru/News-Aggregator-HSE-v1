package com.news.executor;

import java.util.Scanner;

public class CliEngine {
    private final Scanner scanner = new Scanner(System.in);
    private final CommandExecutorService executor;
    private static final String PROMPT = "\u001B[32m>\u001B[0m "; // Green prompt
    private static final String WELCOME_MESSAGE = """
        ╔══════════════════════════════════════════════════════════════╗
        ║                    News Aggregator CLI                       ║
        ║                        Welcome back!                         ║
        ╚══════════════════════════════════════════════════════════════╝
        
        Type 'help' for available commands, 'exit' to quit.
        """;

    public CliEngine(CommandExecutorService executor) {
        this.executor = executor;
    }

    public void run() {
        System.out.println(WELCOME_MESSAGE);

        while (true) {
            System.out.print(PROMPT);
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                continue;
            }

            if (input.equalsIgnoreCase("quit") || input.equalsIgnoreCase("q")) {
                input = "exit";
            }

            long startTime = System.currentTimeMillis();
            executor.execute(input);
            long executionTime = System.currentTimeMillis() - startTime;

            if (executionTime > 1000) {
                System.out.println("\u001B[90m(Executed in " + executionTime + "ms)\u001B[0m");
            }

            if (executor.shouldExit()) {
                break;
            }
        }

        scanner.close();
    }
}