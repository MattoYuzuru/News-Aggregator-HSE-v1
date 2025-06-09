package com.news.executor;

import com.news.model.ParsedCommand;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandParserTest {

    @Test
    void parseSimpleCommand() {
        // Act
        ParsedCommand result = CommandParser.parse("help");

        // Assert
        assertEquals("help", result.getName());
        assertTrue(result.getOptions().isEmpty());
    }

    @Test
    void parseCommandWithSingleFlag() {
        // Act
        ParsedCommand result = CommandParser.parse("parse --verbose");

        // Assert
        assertEquals("parse", result.getName());
        assertTrue(result.hasOption("verbose"));
        assertEquals(List.of("true"), result.getOptionValues("verbose"));
    }

    @Test
    void parseCommandWithSingleValueOption() {
        // Act
        ParsedCommand result = CommandParser.parse("search --title Test");

        // Assert
        assertEquals("search", result.getName());
        assertTrue(result.hasOption("title"));
        assertEquals(List.of("Test"), result.getOptionValues("title")); // Split by spaces
    }

    @Test
    void parseCommandWithMultipleValues() {
        // Act
        ParsedCommand result = CommandParser.parse("export --format json --tags tech news politics");

        // Assert
        assertEquals("export", result.getName());
        assertTrue(result.hasOption("format"));
        assertEquals(List.of("json"), result.getOptionValues("format"));
        assertTrue(result.hasOption("tags"));
        assertEquals(List.of("tech", "news", "politics"), result.getOptionValues("tags"));
    }

    @Test
    void parseCommandWithMultipleFlags() {
        // Act
        ParsedCommand result = CommandParser.parse("parse --verbose --force --dry-run");

        // Assert
        assertEquals("parse", result.getName());
        assertTrue(result.hasOption("verbose"));
        assertTrue(result.hasOption("force"));
        assertTrue(result.hasOption("dry-run"));
        assertEquals(List.of("true"), result.getOptionValues("verbose"));
        assertEquals(List.of("true"), result.getOptionValues("force"));
        assertEquals(List.of("true"), result.getOptionValues("dry-run"));
    }

    @Test
    void parseCommandWithMixedOptions() {
        // Act
        ParsedCommand result = CommandParser.parse("enrich --source bbc --limit 10 --verbose");

        // Assert
        assertEquals("enrich", result.getName());
        assertTrue(result.hasOption("source"));
        assertEquals(List.of("bbc"), result.getOptionValues("source"));
        assertTrue(result.hasOption("limit"));
        assertEquals(List.of("10"), result.getOptionValues("limit"));
        assertTrue(result.hasOption("verbose"));
        assertEquals(List.of("true"), result.getOptionValues("verbose"));
    }

    @Test
    void parseCommandWithExtraSpaces() {
        // Act
        ParsedCommand result = CommandParser.parse("  parse   --source   bbc   cnn  --verbose  ");

        // Assert
        assertEquals("parse", result.getName());
        assertTrue(result.hasOption("source"));
        assertEquals(List.of("bbc", "cnn"), result.getOptionValues("source"));
        assertTrue(result.hasOption("verbose"));
    }

    @Test
    void parseCommandWithEmptyOptionValue() {
        // Act
        ParsedCommand result = CommandParser.parse("search --title --author John");

        // Assert
        assertEquals("search", result.getName());
        assertTrue(result.hasOption("title"));
        assertEquals(List.of("true"), result.getOptionValues("title")); // No value = flag
        assertTrue(result.hasOption("author"));
        assertEquals(List.of("John"), result.getOptionValues("author"));
    }

    @Test
    void parseCommandWithOptionAtEnd() {
        // Act
        ParsedCommand result = CommandParser.parse("export --format");

        // Assert
        assertEquals("export", result.getName());
        assertTrue(result.hasOption("format"));
        assertEquals(List.of("true"), result.getOptionValues("format"));
    }

    @Test
    void parseComplexRealWorldCommand() {
        // Act
        ParsedCommand result = CommandParser.parse("cron --schedule 0 */6 * * * --sources bbc cnn reuters --enrich --limit 100");

        // Assert
        assertEquals("cron", result.getName());
        assertTrue(result.hasOption("schedule"));
        assertEquals(List.of("0", "*/6", "*", "*", "*"), result.getOptionValues("schedule"));
        assertTrue(result.hasOption("sources"));
        assertEquals(List.of("bbc", "cnn", "reuters"), result.getOptionValues("sources"));
        assertTrue(result.hasOption("enrich"));
        assertEquals(List.of("true"), result.getOptionValues("enrich"));
        assertTrue(result.hasOption("limit"));
        assertEquals(List.of("100"), result.getOptionValues("limit"));
    }
}