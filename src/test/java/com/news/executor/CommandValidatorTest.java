package com.news.executor;

import com.news.executor.spec.CommandSpec;
import com.news.executor.spec.OptionSpec;
import com.news.executor.validation.CommandValidator;
import com.news.executor.validation.ValidationResult;
import com.news.model.ParsedCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CommandValidatorTest {

    private CommandValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CommandValidator();
    }

    @Test
    void validateValidCommand() {
        // Arrange
        CommandSpec spec = new CommandSpec.Builder()
                .name("test")
                .description("Test command")
                .options(Set.of(
                        OptionSpec.flag("verbose", "Verbose output"),
                        OptionSpec.withSingleArg("source", "Source name", OptionSpec.OptionType.STRING)
                ))
                .build();

        ParsedCommand command = new ParsedCommand("test", Map.of(
                "verbose", List.of("true"),
                "source", List.of("bbc")
        ));

        // Act
        ValidationResult result = validator.validate(command, spec);

        // Assert
        assertTrue(result.isValid());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    void validateMissingRequiredOption() {
        // Arrange
        CommandSpec spec = new CommandSpec.Builder()
                .name("test")
                .description("Test command")
                .options(Set.of(
                        OptionSpec.withSingleArg("required", "Required option", OptionSpec.OptionType.STRING)
                ))
                .requiredOptions(Set.of("required"))
                .build();

        ParsedCommand command = new ParsedCommand("test", Map.of());

        // Act
        ValidationResult result = validator.validate(command, spec);

        // Assert
        assertFalse(result.isValid());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().getFirst().contains("Missing required option '--required'"));
    }

    @Test
    void validateUnknownOption() {
        // Arrange
        CommandSpec spec = new CommandSpec.Builder()
                .name("test")
                .description("Test command")
                .options(Set.of(
                        OptionSpec.flag("verbose", "Verbose output")
                ))
                .build();

        ParsedCommand command = new ParsedCommand("test", Map.of(
                "unknown", List.of("true")
        ));

        // Act
        ValidationResult result = validator.validate(command, spec);

        // Assert
        assertFalse(result.isValid());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().getFirst().contains("Unknown option '--unknown'"));
    }

    @Test
    void validateMutuallyExclusiveOptions() {
        // Arrange
        CommandSpec spec = new CommandSpec.Builder()
                .name("test")
                .description("Test command")
                .options(Set.of(
                        OptionSpec.flag("all", "All items"),
                        OptionSpec.withSingleArg("id", "Specific ID", OptionSpec.OptionType.INTEGER)
                ))
                .mutuallyExclusiveGroups(Set.of(Set.of("all", "id")))
                .build();

        ParsedCommand command = new ParsedCommand("test", Map.of(
                "all", List.of("true"),
                "id", List.of("123")
        ));

        // Act
        ValidationResult result = validator.validate(command, spec);

        // Assert
        assertFalse(result.isValid());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().getFirst().contains("Cannot use options together"));
    }

    @Test
    void validateIntegerOptionWithInvalidValue() {
        // Arrange
        CommandSpec spec = new CommandSpec.Builder()
                .name("test")
                .description("Test command")
                .options(Set.of(
                        OptionSpec.withSingleArg("limit", "Limit number", OptionSpec.OptionType.INTEGER)
                ))
                .build();

        ParsedCommand command = new ParsedCommand("test", Map.of(
                "limit", List.of("not-a-number")
        ));

        // Act
        ValidationResult result = validator.validate(command, spec);

        // Assert
        assertFalse(result.isValid());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().getFirst().contains("requires integer value"));
    }

    @Test
    void validateOptionWithTooFewArguments() {
        // Arrange
        CommandSpec spec = new CommandSpec.Builder()
                .name("test")
                .description("Test command")
                .options(Set.of(
                        OptionSpec.withMultipleArgs("sources", "Source names", 2, 5, OptionSpec.OptionType.STRING)
                ))
                .build();

        ParsedCommand command = new ParsedCommand("test", Map.of(
                "sources", List.of("bbc") // Need at least 2
        ));

        // Act
        ValidationResult result = validator.validate(command, spec);

        // Assert
        assertFalse(result.isValid());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().getFirst().contains("requires at least 2 argument(s), got 1"));
    }

    @Test
    void validateOptionWithTooManyArguments() {
        // Arrange
        CommandSpec spec = new CommandSpec.Builder()
                .name("test")
                .description("Test command")
                .options(Set.of(
                        OptionSpec.withSingleArg("source", "Source name", OptionSpec.OptionType.STRING)
                ))
                .build();

        ParsedCommand command = new ParsedCommand("test", Map.of(
                "source", List.of("bbc", "cnn", "reuters") // Max 1 allowed
        ));

        // Act
        ValidationResult result = validator.validate(command, spec);

        // Assert
        assertFalse(result.isValid());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().getFirst().contains("accepts at most 1 argument(s), got 3"));
    }

    @Test
    void validateFlagWithArguments() {
        // Arrange
        CommandSpec spec = new CommandSpec.Builder()
                .name("test")
                .description("Test command")
                .options(Set.of(
                        OptionSpec.flag("verbose", "Verbose output")
                ))
                .build();

        ParsedCommand command = new ParsedCommand("test", Map.of(
                "verbose", List.of("some", "args")
        ));

        // Act
        ValidationResult result = validator.validate(command, spec);

        // Assert
        assertFalse(result.isValid());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().getFirst().contains("Flag '--verbose' should not have arguments"));
    }

    @Test
    void validateMultipleErrors() {
        // Arrange
        CommandSpec spec = new CommandSpec.Builder()
                .name("test")
                .description("Test command")
                .options(Set.of(
                        OptionSpec.withSingleArg("required", "Required option", OptionSpec.OptionType.INTEGER)
                ))
                .requiredOptions(Set.of("required"))
                .build();

        ParsedCommand command = new ParsedCommand("test", Map.of(
                "unknown", List.of("true")
        ));

        // Act
        ValidationResult result = validator.validate(command, spec);

        // Assert
        assertFalse(result.isValid());
        assertEquals(2, result.getErrors().size());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("Unknown option")));
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("Missing required option")));
    }
}