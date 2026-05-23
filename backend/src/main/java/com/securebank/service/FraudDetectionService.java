package com.securebank.service;

import com.securebank.entity.Account;
import com.securebank.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FraudDetectionService {

    private final TransactionRepository transactionRepository;

    @Value("${app.fraud.max-daily-transfer}")
    private BigDecimal maxDaily;

    @Value("${app.fraud.suspicious-amount-threshold}")
    private BigDecimal suspiciousThreshold;

    public FraudResult analyze(Account account, BigDecimal amount) {

        double score = 0;
        StringBuilder reason = new StringBuilder();

        // High single-amount check
        if (amount.compareTo(suspiciousThreshold) > 0) {
            score += 0.3;
            reason.append("High amount; ");
        }

        // Daily cumulative limit check
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();

        BigDecimal dailyTotal = transactionRepository
                .sumAmountBySourceAccountAndCreatedAtAfter(account, startOfDay);

        if (dailyTotal != null && dailyTotal.add(amount).compareTo(maxDaily) > 0) {
            score += 0.4;
            reason.append("Daily limit breach; ");
        }

        // Rapid repeated transactions check
        long recentCount = transactionRepository
                .countBySourceAccountAndCreatedAtAfter(
                        account,
                        LocalDateTime.now().minusMinutes(10));

        if (recentCount >= 5) {
            score += 0.3;
            reason.append("Rapid transactions; ");
        }

        boolean flagged = score >= 0.7;

        return new FraudResult(score, flagged, reason.toString().trim());
    }

    public record FraudResult(double score, boolean flagged, String reason) {}
}
