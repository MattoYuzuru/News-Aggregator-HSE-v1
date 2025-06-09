package com.news.executor;

import com.news.executor.impl.system.ExitCommand;
import com.news.model.ParsedCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommandExecutorServiceTest {

    @Mock
    private CommandRegistry registry;
    @Mock
    private Command mockCommand;
    @Mock
    private ExitCommand exitCommand;

    private CommandExecutorService executor;
    private ByteArrayOutputStream outputStream;

    @BeforeEach
    void setUp() {
        executor = new CommandExecutorService(registry);

        // Capture System.out and System.err
        outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        System.setOut(new PrintStream(outputStream));
        System.setErr(new PrintStream(outputStream));
    }

    @Test
    void executeValidCommand() {
        // Arrange
        when(registry.getCommand("test")).thenReturn(Optional.of(mockCommand));

        // Act
        executor.execute("test --option value");

        // Assert
        verify(mockCommand).execute(any(ParsedCommand.class));
        assertFalse(executor.shouldExit());
    }

    @Test
    void executeUnknownCommand() {
        // Arrange
        when(registry.getCommand("unknown")).thenReturn(Optional.empty());
        when(registry.getAvailableCommands()).thenReturn(Set.of());

        // Act
        executor.execute("unknown");

        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("Unknown command: unknown"));
        assertTrue(output.contains("Available commands: "));
    }

    @Test
    void executeEmptyInput() {
        // Act
        executor.execute("");

        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("Empty command"));
    }

    @Test
    void executeNullInput() {
        // Act
        executor.execute(null);

        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("Empty command"));
    }

    @Test
    void executeBlankInput() {
        // Act
        executor.execute("   ");

        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("Empty command"));
    }

    @Test
    void executeCommandWithException() {
        // Arrange
        when(registry.getCommand("test")).thenReturn(Optional.of(mockCommand));
        doThrow(new RuntimeException("Command failed")).when(mockCommand).execute(any());

        // Act
        executor.execute("test");

        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("Error executing command 'test': Command failed"));
    }

    @Test
    void executeInvalidCommandSyntax() {
        // Act - попробуем что-то, что может сломать парсер
        executor.execute("test --");

        // Assert
        String output = outputStream.toString();
        // Должен либо обработать корректно, либо показать ошибку парсинга
        assertFalse(output.isEmpty());
    }
}