package com.infy.bankingApplication.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.infy.bankingApplication.entity.AuditLog;

public interface AuditLogService {
	
	void log(
            String action,
            String accountNumber,
            String targetAccount,
            BigDecimal amount,
            String status);
	
	void log(
            String action,
            String accountNumber,
            String targetAccount,
            BigDecimal amount,
            String status,
            String username);
	
	void logFailedTransaction(
	        String action,
	        String accountNumber,
	        String targetAccount,
	        BigDecimal amount,
	        String username);
	
	 Page<AuditLog> getAllAuditLogs(Pageable pageable);

	    Page<AuditLog> getAuditLogsByAccount(
	            String accountNumber,
	            Pageable pageable);
	    
	    Page<AuditLog> getAuditLogsByDateRange(
	            LocalDateTime startDate,
	            LocalDateTime endDate,
	            Pageable pageable);
	    
	    Page<AuditLog> getAuditLogsByFilters(
	            String action,
	            String status,
	            LocalDateTime startDate,
	            LocalDateTime endDate,
	            Pageable pageable);
	    

Page<AuditLog> getAuditLogsByUsername(
        String username,
        Pageable pageable);


Page<AuditLog> getAuditLogsByUsernameAndAccount(
        String username,
        String accountNumber,
        Pageable pageable);

void logDltReplay(
        int partition,
        long offset,
        String transactionReference,
        String status);

}
