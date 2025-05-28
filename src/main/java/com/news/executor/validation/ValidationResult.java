package com.news.executor.validation;

import lombok.Getter;

import java.util.List;

@Getter
public class ValidationResult {
    private final boolean valid;
    private final List<String> errors;

    public ValidationResult(boolean valid, List<String> errors) {
        this.valid = valid;
        this.errors = errors;
    }
}
