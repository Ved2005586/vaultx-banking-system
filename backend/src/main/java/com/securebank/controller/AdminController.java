package com.securebank.controller;

import com.securebank.dto.*;
import com.securebank.entity.*;
import com.securebank.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    @GetMapping("/users")
    public ResponseEntity<Page<UserSummaryResponse>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(
                userRepository.findAll(PageRequest.of(page, size))
                        .map(this::mapUser));
    }

    @PatchMapping("/users/{id}/toggle-lock")
    public ResponseEntity<Void> toggleLock(@PathVariable String id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setAccountNonLocked(!user.isAccountNonLocked());
        userRepository.save(user);

        return ResponseEntity.noContent().build();
    }

    private UserSummaryResponse mapUser(User u) {
        return new UserSummaryResponse(
                u.getId(),
                u.getUsername(),
                u.getEmail(),
                u.getFullName(),
                u.getRole().name(),
                u.isEnabled(),
                !u.isAccountNonLocked(),
                u.getLastLogin(),
                u.getCreatedAt()
        );
    }
}