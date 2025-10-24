package com.nsu.issue_tracker.authorization.exception;

import java.util.Map;

public record ErrorResponse(
        String message,
        Map<String, String> errors
) {}
