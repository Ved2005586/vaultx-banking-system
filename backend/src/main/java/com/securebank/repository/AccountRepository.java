package com.securebank.repository;

import com.securebank.entity.Account;
import com.securebank.entity.User;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, String> {

    List<Account> findByUser(User user);

    Optional<Account> findByAccountNumber(String accountNumber);

    @Query("SELECT a FROM Account a WHERE a.id = :id AND a.user.id = :userId")
    Optional<Account> findByIdAndUserId(@Param("id") String id,
                                        @Param("userId") String userId);

    @Query("SELECT a FROM Account a WHERE a.id = :id AND a.user.id = :userId AND a.status = :status")
    Optional<Account> findByIdAndUserIdAndStatus(@Param("id") String id,
                                                 @Param("userId") String userId,
                                                 @Param("status") Account.AccountStatus status);
}