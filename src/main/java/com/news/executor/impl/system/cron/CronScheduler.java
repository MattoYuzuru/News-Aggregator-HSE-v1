package com.news.executor.impl.system.cron;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class CronScheduler {
    private ScheduledExecutorService scheduler;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicBoolean shouldStop = new AtomicBoolean(false);
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public CronScheduler() {
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    public void schedule(CronSchedule schedule, Runnable task, String mode, String sources) {
        if (isRunning.get()) {
            throw new IllegalStateException("Scheduler is already running. Stop the current job first.");
        }

        if (scheduler.isShutdown() || scheduler.isTerminated()) {
            scheduler = Executors.newScheduledThreadPool(1);
        }

        shouldStop.set(false);
        long initialDelay = schedule.calculateInitialDelaySeconds();

        System.out.printf("   Starting cron job with %d second intervals%n", schedule.periodSeconds());
        System.out.printf("   Initial delay: %d seconds%n", initialDelay);
        System.out.printf("   Mode: %s%n", mode);
        System.out.printf("   Sources: %s%n", sources);
        System.out.printf("   Next execution: %s%n", schedule.getNextExecutionTimeFormatted());
        System.out.println("  Use 'cron --stop' to stop");

        Runnable wrappedTask = () -> {
            if (shouldStop.get()) return;

            System.out.printf("%n [%s] Starting scheduled parsing...%n",
                    LocalDateTime.now().format(TIME_FORMAT));

            try {
                task.run();
                System.out.printf("[%s] Scheduled parsing completed%n",
                        LocalDateTime.now().format(TIME_FORMAT));
            } catch (Exception e) {
                System.err.printf("[%s] Error during scheduled parsing: %s%n",
                        LocalDateTime.now().format(TIME_FORMAT),
                        e.getMessage());
            }
        };

        isRunning.set(true);
        scheduler.scheduleAtFixedRate(wrappedTask, initialDelay, schedule.periodSeconds(), TimeUnit.SECONDS);
    }

    public void stop() {
        if (!isRunning.get()) {
            System.out.println("No cron job is currently running");
            return;
        }

        shouldStop.set(true);
        isRunning.set(false);

        if (!scheduler.isShutdown()) {
            System.out.println("🛑 Stopping cron job...");
            scheduler.shutdown();

            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                    System.out.println("Force shutdown completed");
                } else {
                    System.out.println("Cron job stopped gracefully");
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    public boolean isRunning() {
        return isRunning.get();
    }

    public boolean shouldStop() {
        return shouldStop.get();
    }
}