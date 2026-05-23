package com.securebank.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record DepositRequest(

        @NotBlank
        String accountId,

        @NotNull
        @DecimalMin(value = "0.01")
        BigDecimal amount
) {}