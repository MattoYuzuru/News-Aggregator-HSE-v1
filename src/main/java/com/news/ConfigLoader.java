package com.news;

import java.io.FileInputStream;
import java.util.Properties;

public class ConfigLoader {

    public static String getApiKey() {
        try {
            Properties props = new Properties();
            FileInputStream in = new FileInputStream("config.properties");
            props.load(in);
            return props.getProperty("HF_API_KEY");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

