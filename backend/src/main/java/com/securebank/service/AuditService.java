package com.securebank.service;

import com.securebank.entity.AuditLog;
import com.securebank.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Async
    public void log(String userId, String action, String resourceType,
                    String resourceId, String ipAddress, String userAgent,
                    boolean success, String details) {

        AuditLog.Severity severity = determineSeverity(action, success);

        AuditLog entry = AuditLog.builder()
                .userId(userId)
                .action(action)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .severity(severity)
                .details(details)
                .success(success)
                .build();

        auditLogRepository.save(entry);
    }

    private AuditLog.Severity determineSeverity(String action, boolean success) {
        if (!success) return AuditLog.Severity.WARNING;
        if (action.contains("FRAUD") || action.contains("FLAGGED") || action.contains("LOCK")) {
            return AuditLog.Severity.CRITICAL;
        }
        return AuditLog.Severity.INFO;
    }
}