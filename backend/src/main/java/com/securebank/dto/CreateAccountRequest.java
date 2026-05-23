package com.securebank.dto;

import jakarta.validation.constraints.NotNull;

public record CreateAccountRequest(

        @NotNull(message = "Account type is required")
        AccountType accountType
) {
    public enum AccountType {
        CHECKING,
        SAVINGS,
        INVESTMENT
    }
}