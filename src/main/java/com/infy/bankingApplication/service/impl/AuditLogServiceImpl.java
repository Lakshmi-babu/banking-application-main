package com.infy.bankingApplication.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.infy.bankingApplication.entity.AuditLog;
import com.infy.bankingApplication.repository.AuditLogRepository;

import com.infy.bankingApplication.service.AuditLogService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Service
public class AuditLogServiceImpl implements AuditLogService {

	private final AuditLogRepository auditLogRepository;

    private static final Logger log =
            LoggerFactory.getLogger(AuditLogServiceImpl.class);

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    // Log an audit event using the currently authenticated username.
    @Override
    @Transactional
    public void log(
            String action,
            String accountNumber,
            String targetAccount,
            BigDecimal amount,
            String status) {

        String username = getCurrentUsername();

        log.info("Audit action: {}", action);
        log.info("Audit username selected: {}", username);

        saveAuditLog(
                action,
                accountNumber,
                targetAccount,
                amount,
                status,
                username);
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logDltReplay(
            int partition,
            long offset,
            String transactionReference,
            String status) {

        AuditLog auditLog = new AuditLog();

        auditLog.setAction("DLT_REPLAY");
        auditLog.setUsername(getCurrentUsername());
        auditLog.setStatus(status);
        auditLog.setCreatedAt(LocalDateTime.now());

        auditLog.setDetails(
                "partition=" + partition
                + ", offset=" + offset
                + ", transactionReference="
                + (transactionReference == null
                        ? "UNKNOWN"
                        : transactionReference));

        auditLogRepository.save(auditLog);
    }

    // Log an audit event using an explicitly supplied username.
    @Override
    @Transactional
    public void log(
            String action,
            String accountNumber,
            String targetAccount,
            BigDecimal amount,
            String status,
            String username) {

        saveAuditLog(
                action,
                accountNumber,
                targetAccount,
                amount,
                status,
                username);
    }

    // Save failed-transaction audit records in a separate transaction.
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logFailedTransaction(
            String action,
            String accountNumber,
            String targetAccount,
            BigDecimal amount,
            String username) {

        saveAuditLog(
                action,
                accountNumber,
                targetAccount,
                amount,
                "FAILED",
                username);
    }

    // Get the current authenticated username.
    private String getCurrentUsername() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            return "SYSTEM";
        }

        return authentication.getName();
    }

    // Create and persist an audit record.
    private void saveAuditLog(
            String action,
            String accountNumber,
            String targetAccount,
            BigDecimal amount,
            String status,
            String username) {

        AuditLog auditLog = new AuditLog();

        auditLog.setAction(action);
        auditLog.setAccountNumber(accountNumber);
        auditLog.setTargetAccount(targetAccount);
        auditLog.setAmount(amount);
        auditLog.setStatus(status);

        auditLog.setUsername(
                username == null || username.isBlank()
                        ? "SYSTEM"
                        : username);

        auditLog.setCreatedAt(LocalDateTime.now());

        auditLogRepository.save(auditLog);
    }

    // Retrieve all audit logs with pagination.
    @Override
    @Transactional(readOnly = true)
    public Page<AuditLog> getAllAuditLogs(Pageable pageable) {

        return auditLogRepository
                .findAllByOrderByCreatedAtDesc(pageable);
    }

    // Retrieve audit logs by account number.
    @Override
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsByAccount(
            String accountNumber,
            Pageable pageable) {

        return auditLogRepository
                .findByAccountNumberOrderByCreatedAtDesc(
                        accountNumber, pageable);
    }

    // Retrieve audit logs within a date range.
    @Override
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsByDateRange(
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {

        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException(
                    "Start date and end date are required");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(
                    "Start date must be before or equal to end date");
        }

        return auditLogRepository
                .findByCreatedAtBetweenOrderByCreatedAtDesc(
                        startDate,
                        endDate,
                        pageable);
    }

    // Retrieve audit logs by action, status, and date range.
    @Override
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsByFilters(
            String action,
            String status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {

        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException(
                    "Start date and end date are required");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(
                    "Start date must be before or equal to end date");
        }

        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException(
                    "Action is required");
        }

        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException(
                    "Status is required");
        }

        String normalizedAction = action.trim().toUpperCase();
        String normalizedStatus = status.trim().toUpperCase();

        if (!Set.of(
                "DEPOSIT",
                "WITHDRAW",
                "TRANSFER",
                "DLT_REPLAY")
                .contains(normalizedAction)) {

            throw new IllegalArgumentException(
                    "Invalid action. Allowed values: DEPOSIT, WITHDRAW, TRANSFER, DLT_REPLAY");
        }

        if (!Set.of("SUCCESS", "FAILED")
                .contains(normalizedStatus)) {
            throw new IllegalArgumentException(
                    "Invalid status. Allowed values: SUCCESS, FAILED");
        }

        return auditLogRepository
                .findByActionAndStatusAndCreatedAtBetweenOrderByCreatedAtDesc(
                        normalizedAction,
                        normalizedStatus,
                        startDate,
                        endDate,
                        pageable);
    }

    // Retrieve audit logs by username.
    @Override
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsByUsername(
            String username,
            Pageable pageable) {

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                    "Username is required");
        }

        return auditLogRepository
                .findByUsernameOrderByCreatedAtDesc(
                        username.trim(),
                        pageable);
    }

    // Retrieve audit logs by username and account number.
    @Override
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsByUsernameAndAccount(
            String username,
            String accountNumber,
            Pageable pageable) {

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                    "Username is required");
        }

        if (accountNumber == null || accountNumber.isBlank()) {
            throw new IllegalArgumentException(
                    "Account number is required");
        }

        return auditLogRepository
                .findByUsernameAndAccountNumberOrderByCreatedAtDesc(
                        username.trim(),
                        accountNumber.trim(),
                        pageable);
    }

}
