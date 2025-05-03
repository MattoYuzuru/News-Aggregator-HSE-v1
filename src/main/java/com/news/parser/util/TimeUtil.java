package com.news.parser.util;

import java.time.*;

public class TimeUtil {
    public static LocalDateTime dateConverter(String time) {
        long timestampMs = Long.parseLong(time);

        Instant instant = Instant.ofEpochMilli(timestampMs);

        ZoneId moscowZone = ZoneId.of("Europe/Moscow");

        return LocalDateTime.ofInstant(instant, moscowZone);
    }

    public static LocalDateTime dateFromString(String time) {
        String[] timeSplit = time.split(" ");
        int timeNumber = Integer.parseInt(timeSplit[0]);
        char timeType = timeSplit[1].charAt(0);

        ZoneId moscowZone = ZoneId.of("Europe/Moscow");
        LocalDateTime currentTime = LocalDateTime.now(moscowZone);

        if (timeType == 'm') { // minutes
            return currentTime.minusMinutes(timeNumber);
        } else if (timeType == 'h') { // hours
            return currentTime.minusHours(timeNumber);
        } else if (timeType == 's') { // seconds
            return currentTime.minusSeconds(timeNumber);
        } else if (timeType == 'd') {
            return currentTime.minusDays(timeNumber);
        } else {
            throw new IllegalArgumentException("Wrong time format: " + time);
        }
    }
}
