package com.securebank.service;

import com.securebank.dto.*;
import com.securebank.entity.*;
import com.securebank.repository.*;
import com.securebank.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final FraudDetectionService fraudDetectionService;
    private final EncryptionUtil encryptionUtil;
    private final AuditService auditService;

    // ─────────────────────────────────────────────
    // TRANSFER
    // ─────────────────────────────────────────────
    @Transactional
    public TransactionResponse transfer(TransferRequest request, String userId, String ipAddress) {

        Account sourceAccount = accountRepository
                .findByIdAndUserIdAndStatus(
                        request.sourceAccountId(),
                        userId,
                        Account.AccountStatus.ACTIVE
                )
                .orElseThrow(() -> new IllegalArgumentException("Source account not found or inactive"));

        Account destinationAccount = accountRepository
                .findByAccountNumber(request.destinationAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("Destination account not found"));

        BigDecimal amount = request.amount();

        if (sourceAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient balance");
        }

        // ─── FRAUD CHECK (FIXED) ─────────────────────
        FraudDetectionService.FraudResult fraud =
                fraudDetectionService.analyze(sourceAccount, amount);

        if (fraud.flagged()) {

            Transaction flaggedTxn = buildTransaction(
                    sourceAccount,
                    request.destinationAccountNumber(),
                    amount,
                    Transaction.TransactionType.TRANSFER,
                    request.description(),
                    ipAddress,
                    fraud
            );

            flaggedTxn.setStatus(Transaction.TransactionStatus.FLAGGED);
            transactionRepository.save(flaggedTxn);

            auditService.log(
                    userId,
                    "TRANSACTION_FLAGGED",
                    "Transaction",
                    flaggedTxn.getId(),
                    ipAddress,
                    null,
                    false,
                    fraud.reason()
            );

            throw new SecurityException(
                    "Transaction blocked due to fraud detection. Score: " +
                            String.format("%.2f", fraud.score())
            );
        }

        // ─── EXECUTE TRANSFER ───────────────────────
        sourceAccount.setBalance(sourceAccount.getBalance().subtract(amount));
        destinationAccount.setBalance(destinationAccount.getBalance().add(amount));

        accountRepository.save(sourceAccount);
        accountRepository.save(destinationAccount);

        Transaction transaction = buildTransaction(
                sourceAccount,
                request.destinationAccountNumber(),
                amount,
                Transaction.TransactionType.TRANSFER,
                request.description(),
                ipAddress,
                fraud
        );

        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        transaction.setBalanceAfter(sourceAccount.getBalance());
        transaction.setProcessedAt(LocalDateTime.now());

        transaction = transactionRepository.save(transaction);

        auditService.log(
                userId,
                "TRANSFER_COMPLETED",
                "Transaction",
                transaction.getId(),
                ipAddress,
                null,
                true,
                "Transferred " + amount
        );

        return mapToResponse(transaction, encryptionUtil);
    }

    // ─────────────────────────────────────────────
    // DEPOSIT
    // ─────────────────────────────────────────────
    @Transactional
    public TransactionResponse deposit(DepositRequest request, String userId, String ipAddress) {

        Account account = accountRepository
                .findByIdAndUserIdAndStatus(
                        request.accountId(),
                        userId,
                        Account.AccountStatus.ACTIVE
                )
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        account.setBalance(account.getBalance().add(request.amount()));
        accountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .referenceId(generateReference())
                .type(Transaction.TransactionType.DEPOSIT)
                .amount(request.amount())
                .sourceAccount(account)
                .encryptedDescription(encryptionUtil.encryptAES("Deposit"))
                .balanceAfter(account.getBalance())
                .status(Transaction.TransactionStatus.COMPLETED)
                .ipAddress(ipAddress)
                .processedAt(LocalDateTime.now())
                .build();

        transaction = transactionRepository.save(transaction);

        return mapToResponse(transaction, encryptionUtil);
    }

    // ─────────────────────────────────────────────
    // HISTORY
    // ─────────────────────────────────────────────
    public Page<TransactionResponse> getTransactionHistory(
            String accountId,
            String userId,
            Pageable pageable
    ) {

        Account account = accountRepository
                .findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        return transactionRepository
                .findBySourceAccountOrderByCreatedAtDesc(account, pageable)
                .map(t -> mapToResponse(t, encryptionUtil));
    }

    // ─────────────────────────────────────────────
    // HELPER: BUILD TRANSACTION
    // ─────────────────────────────────────────────
    private Transaction buildTransaction(
            Account source,
            String destAccountNumber,
            BigDecimal amount,
            Transaction.TransactionType type,
            String description,
            String ipAddress,
            FraudDetectionService.FraudResult fraud
    ) {

        return Transaction.builder()
                .referenceId(generateReference())
                .type(type)
                .amount(amount)
                .sourceAccount(source)
                .destinationAccountNumber(destAccountNumber)
                .encryptedDescription(encryptionUtil.encryptAES(description))
                .fraudScore(fraud.score())
                .flagged(fraud.flagged())
                .flagReason(fraud.reason())
                .ipAddress(ipAddress)
                .build();
    }

    private String generateReference() {
        return "TXN-" +
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 16)
                        .toUpperCase();
    }

    public static TransactionResponse mapToResponse(Transaction t, EncryptionUtil enc) {

        String description;

        try {
            description = enc.decryptAES(t.getEncryptedDescription());
        } catch (Exception e) {
            description = "DECRYPT_ERROR";
        }

        return new TransactionResponse(
                t.getId(),
                t.getReferenceId(),
                t.getType().name(),
                t.getAmount(),
                t.getCurrencyCode(),
                description,
                t.getSourceAccount().getAccountNumber(),
                t.getDestinationAccountNumber(),
                t.getBalanceAfter(),
                t.getStatus().name(),
                t.isFlagged(),
                t.getFraudScore(),
                t.getCreatedAt(),
                t.getProcessedAt()
        );
    }
}