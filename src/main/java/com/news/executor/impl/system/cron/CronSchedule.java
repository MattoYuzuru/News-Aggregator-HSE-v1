package com.news.executor.impl.system.cron;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public record CronSchedule(LocalTime startTime, long periodSeconds) {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static CronSchedule of(int hour, int minute, int second, long periodSeconds) {
        return new CronSchedule(LocalTime.of(hour, minute, second), periodSeconds);
    }

    public long calculateInitialDelaySeconds() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextRun = now.toLocalDate().atTime(startTime);

        if (nextRun.isBefore(now) || nextRun.isEqual(now)) {
            nextRun = nextRun.plusDays(1);
        }

        return ChronoUnit.SECONDS.between(now, nextRun);
    }

    public String getNextExecutionTimeFormatted() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextRun = now.toLocalDate().atTime(startTime);

        if (nextRun.isBefore(now) || nextRun.isEqual(now)) {
            nextRun = nextRun.plusDays(1);
        }

        return nextRun.format(TIME_FORMAT);
    }

    public long getPeriodMinutes() {
        return periodSeconds / 60;
    }
}