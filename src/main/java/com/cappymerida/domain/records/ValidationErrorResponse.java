package com.cappymerida.domain.records;

import java.time.LocalDateTime;
import java.util.Map;

public record ValidationErrorResponse(
        String code,
        String message,
        LocalDateTime timestamp,
        Map<String, String> validationErrors
) {}
