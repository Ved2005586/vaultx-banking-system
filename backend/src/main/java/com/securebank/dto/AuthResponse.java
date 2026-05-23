package com.securebank.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String userId,
        String username,
        String email,
        String fullName,
        String role,
        long expiresAt
) {}