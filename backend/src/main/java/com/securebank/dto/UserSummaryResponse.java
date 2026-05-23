package com.securebank.dto;

import java.time.LocalDateTime;

public record UserSummaryResponse(
        String id,
        String username,
        String email,
        String fullName,
        String role,
        boolean enabled,
        boolean locked,
        LocalDateTime lastLogin,
        LocalDateTime createdAt
) {}