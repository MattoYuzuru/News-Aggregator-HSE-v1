package com.news;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


// save your data before running
class ConfigLoaderTest {

    @TempDir
    Path tempDir;

    Path configFile;

    @Test
    void returnsDbUrlFromConfig() throws IOException {
        writeConfigFile("DB_URL=jdbc:testurl\n");
        System.setProperty("user.dir", tempDir.toString());

        Files.copy(configFile, Paths.get("config.properties"), StandardCopyOption.REPLACE_EXISTING);

        assertEquals("jdbc:testurl", ConfigLoader.getDBUrl());

        Files.deleteIfExists(Paths.get("config.properties"));
    }

    @Test
    void returnsDbUserFromConfig() throws IOException {
        writeConfigFile("DB_USERNAME=testuser\n");
        Files.copy(configFile, Paths.get("config.properties"), StandardCopyOption.REPLACE_EXISTING);

        assertEquals("testuser", ConfigLoader.getDBUser());

        Files.deleteIfExists(Paths.get("config.properties"));
    }

    @Test
    void returnsDbPasswordFromConfig() throws IOException {
        writeConfigFile("DB_PASSWORD=testpass\n");
        Files.copy(configFile, Paths.get("config.properties"), StandardCopyOption.REPLACE_EXISTING);

        assertEquals("testpass", ConfigLoader.getDBPassword());

        Files.deleteIfExists(Paths.get("config.properties"));
    }

    @Test
    void returnsNullWhenConfigFileIsMissing() {
        assertNull(ConfigLoader.getDBUrl());
        assertNull(ConfigLoader.getDBUser());
        assertNull(ConfigLoader.getDBPassword());
    }

    @Test
    void returnsNullWhenKeyIsMissing() throws IOException {
        writeConfigFile("");
        Files.copy(configFile, Paths.get("config.properties"), StandardCopyOption.REPLACE_EXISTING);

        assertNull(ConfigLoader.getDBUrl());
        assertNull(ConfigLoader.getDBUser());
        assertNull(ConfigLoader.getDBPassword());

        Files.deleteIfExists(Paths.get("config.properties"));
    }

    void writeConfigFile(String contents) throws IOException {
        Files.writeString(configFile, contents);
    }
}