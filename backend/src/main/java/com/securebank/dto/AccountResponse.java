package com.securebank.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountResponse(
        String id,
        String accountNumber,
        String accountType,
        BigDecimal balance,
        String currencyCode,
        String status,
        LocalDateTime createdAt
) {}