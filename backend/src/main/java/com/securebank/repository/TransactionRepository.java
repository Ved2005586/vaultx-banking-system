package com.securebank.repository;

import com.securebank.entity.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface TransactionRepository extends JpaRepository<Transaction, String> {

    Page<Transaction> findBySourceAccountOrderByCreatedAtDesc(Account account, Pageable pageable);

    long countBySourceAccountAndCreatedAtAfter(Account account, LocalDateTime after);

    @Query("SELECT COALESCE(SUM(t.amount),0) FROM Transaction t " +
            "WHERE t.sourceAccount = :account AND t.createdAt > :after AND t.status = 'COMPLETED'")
    BigDecimal sumAmountBySourceAccountAndCreatedAtAfter(
            @Param("account") Account account,
            @Param("after") LocalDateTime after);

    long countBySourceAccountAndAmountAndCreatedAtAfter(
            Account account, BigDecimal amount, LocalDateTime after);

    Page<Transaction> findByFlaggedTrueOrderByCreatedAtDesc(Pageable pageable);
}