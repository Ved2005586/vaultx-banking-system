package com.securebank.repository;

import com.securebank.entity.AuditLog;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, String> {

    Page<AuditLog> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    Page<AuditLog> findBySeverityOrderByCreatedAtDesc(AuditLog.Severity severity, Pageable pageable);
}