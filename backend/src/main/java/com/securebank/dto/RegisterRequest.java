package com.securebank.dto;

import jakarta.validation.constraints.*;

public record RegisterRequest(

        @NotBlank
        @Size(min = 3, max = 50)
        String username,

        @NotBlank
        @Email
        String email,

        @NotBlank
        @Size(min = 8, max = 100)
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$",
                message = "Password must contain uppercase, lowercase, number, special character"
        )
        String password,

        @NotBlank
        String fullName,

        String phoneNumber
) {}