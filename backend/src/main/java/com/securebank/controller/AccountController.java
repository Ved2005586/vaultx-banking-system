package com.securebank.controller;

import com.securebank.dto.*;
import com.securebank.entity.*;
import com.securebank.repository.AccountRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountRepository accountRepository;

    @GetMapping
    public ResponseEntity<List<AccountResponse>> myAccounts(@AuthenticationPrincipal User user) {

        List<AccountResponse> accounts = accountRepository.findByUser(user)
                .stream()
                .map(this::mapToResponse)
                .toList();

        return ResponseEntity.ok(accounts);
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody CreateAccountRequest request,
            @AuthenticationPrincipal User user) {

        Account account = Account.builder()
                .accountNumber(generateAccountNumber())
                .accountType(Account.AccountType.valueOf(request.accountType().name()))
                .user(user)
                .build();

        account = accountRepository.save(account);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapToResponse(account));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccount(
            @PathVariable String id,
            @AuthenticationPrincipal User user) {

        Account account = accountRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        return ResponseEntity.ok(mapToResponse(account));
    }

    private AccountResponse mapToResponse(Account a) {
        return new AccountResponse(
                a.getId(),
                a.getAccountNumber(),
                a.getAccountType().name(),
                a.getBalance(),
                a.getCurrencyCode(),
                a.getStatus().name(),
                a.getCreatedAt()
        );
    }

    private String generateAccountNumber() {
        return String.valueOf(1000000000000L + new Random().nextLong(9000000000000L));
    }
}