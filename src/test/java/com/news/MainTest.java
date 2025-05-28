package com.news;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class MainTest {
    @Test
    void mainRunsWithoutException() {
        assertDoesNotThrow(() -> Main.main(new String[]{}));
    }
}