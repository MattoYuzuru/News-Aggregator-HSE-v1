package com.news;

import java.io.FileInputStream;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class ConfigLoader {

    public static final int AI_SUPPLEMENT_TIME_PER_ARTICLE = 5;
    public static final Duration TIMEOUT = Duration.ofSeconds(15);
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    public static String getDBUrl() {
        try {
            Properties props = new Properties();
            FileInputStream in = new FileInputStream("config.properties");
            props.load(in);
            return props.getProperty("DB_URL");
        } catch (Exception e) {
            System.out.println("Cannot find db url " + e.getMessage());
            return null;
        }
    }

    public static String getDBUser() {
        try {
            Properties props = new Properties();
            FileInputStream in = new FileInputStream("config.properties");
            props.load(in);
            return props.getProperty("DB_USERNAME");
        } catch (Exception e) {
            System.out.println("Cannot find db username " + e.getMessage());
            return null;
        }
    }

    public static String getDBPassword() {
        try {
            Properties props = new Properties();
            FileInputStream in = new FileInputStream("config.properties");
            props.load(in);
            return props.getProperty("DB_PASSWORD");
        } catch (Exception e) {
            System.out.println("Cannot find db password " + e.getMessage());
            return null;
        }
    }

}
