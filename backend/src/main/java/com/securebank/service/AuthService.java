package com.securebank.service;

import com.securebank.dto.*;
import com.securebank.entity.User;
import com.securebank.repository.UserRepository;
import com.securebank.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final JwtTokenProvider jwt;

    // ── Register ────────────────────────────────────────

    public AuthResponse register(RegisterRequest request) {

        if (userRepo.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username already taken");
        }
        if (userRepo.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .fullName(request.fullName())
                .phoneNumber(request.phoneNumber())
                .password(encoder.encode(request.password()))
                .role(User.Role.USER)
                .enabled(true)
                .accountNonLocked(true)
                .build();

        userRepo.save(user);

        return buildAuthResponse(user);
    }

    // ── Login ───────────────────────────────────────────

    public AuthResponse login(LoginRequest request, String ipAddress) {

        User user = userRepo
                .findByUsernameOrEmail(request.usernameOrEmail(), request.usernameOrEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!user.isEnabled()) {
            throw new DisabledException("Account is disabled");
        }
        if (!user.isAccountNonLocked()) {
            throw new LockedException("Account is locked");
        }
        if (!encoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        return buildAuthResponse(user);
    }

    // ── Refresh Token ────────────────────────────────────

    public AuthResponse refreshToken(String refreshToken) {

        if (!jwt.validate(refreshToken)) {
            throw new IllegalArgumentException("Invalid or expired refresh token");
        }

        String username = jwt.getUsername(refreshToken);
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return new AuthResponse(
                jwt.generateAccessToken(user),
                refreshToken,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().name(),
                System.currentTimeMillis() + 900_000L
        );
    }

    // ── Logout ───────────────────────────────────────────

    public void logout() {
        // Token blacklisting can be implemented here
    }

    // ── Helper ───────────────────────────────────────────

    private AuthResponse buildAuthResponse(User user) {
        return new AuthResponse(
                jwt.generateAccessToken(user),
                jwt.generateRefreshToken(user),
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().name(),
                System.currentTimeMillis() + 900_000L
        );
    }
}
