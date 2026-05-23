package com.securebank.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        String id,
        String referenceId,
        String type,
        BigDecimal amount,
        String currencyCode,
        String description,
        String sourceAccountNumber,
        String destinationAccountNumber,
        BigDecimal balanceAfter,
        String status,
        boolean flagged,
        double fraudScore,
        LocalDateTime createdAt,
        LocalDateTime processedAt
) {}