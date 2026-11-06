package com.infy.bankingApplication.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.infy.bankingApplication.entity.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByAccountNumberOrderByCreatedAtDesc(
            String accountNumber);
    
    Page<AuditLog> findAllByOrderByCreatedAtDesc(
            Pageable pageable);

    Page<AuditLog> findByAccountNumberOrderByCreatedAtDesc(
            String accountNumber,
            Pageable pageable);
    
    Page<AuditLog> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable);
    
    Page<AuditLog> findByActionAndStatusAndCreatedAtBetweenOrderByCreatedAtDesc(
            String action,
            String status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable);
    
    Page<AuditLog> findByUsernameOrderByCreatedAtDesc(
            String username,
            Pageable pageable);
    
    Page<AuditLog> findByUsernameAndAccountNumberOrderByCreatedAtDesc(
            String username,
            String accountNumber,
            Pageable pageable);

}
