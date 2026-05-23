package com.securebank.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FraudAlertResponse(
        String transactionId,
        String referenceId,
        String accountNumber,
        BigDecimal amount,
        double fraudScore,
        String flagReason,
        String status,
        LocalDateTime createdAt
) {}