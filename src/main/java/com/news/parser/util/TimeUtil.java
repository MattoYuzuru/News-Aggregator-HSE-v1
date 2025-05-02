package com.news.parser.util;

import java.time.*;

public class TimeUtil {
    public static LocalDateTime dateConverter(String time) {
        long timestampMs = Long.parseLong(time);

        Instant instant = Instant.ofEpochMilli(timestampMs);

        ZoneId moscowZone = ZoneId.of("Europe/Moscow");

        return LocalDateTime.ofInstant(instant, moscowZone);
    }
}
