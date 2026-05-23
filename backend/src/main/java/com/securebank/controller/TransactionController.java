package com.securebank.controller;

import com.securebank.dto.*;
import com.securebank.entity.User;
import com.securebank.service.TransactionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(
            @Valid @RequestBody TransferRequest request,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpRequest) {

        return ResponseEntity.ok(
                transactionService.transfer(request, user.getId(),
                        extractClientIp(httpRequest)));
    }

    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(
            @Valid @RequestBody DepositRequest request,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpRequest) {

        return ResponseEntity.ok(
                transactionService.deposit(request, user.getId(),
                        extractClientIp(httpRequest)));
    }

    @GetMapping("/history/{accountId}")
    public ResponseEntity<Page<TransactionResponse>> history(
            @PathVariable String accountId,
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());

        return ResponseEntity.ok(
                transactionService.getTransactionHistory(accountId, user.getId(), pageable));
    }

    private String extractClientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        return (xff != null) ? xff.split(",")[0] : req.getRemoteAddr();
    }
}