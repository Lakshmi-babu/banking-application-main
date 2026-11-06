package com.infy.bankingApplication.controller;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Sort;

import com.infy.bankingApplication.dto.TransactionResponse;
import com.infy.bankingApplication.dto.TransactionSummaryResponse;
import com.infy.bankingApplication.entity.BankTransaction;
import com.infy.bankingApplication.service.TransactionService;


@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
	
	private final TransactionService transactionService;

    public TransactionController(
            TransactionService transactionService) {

        this.transactionService = transactionService;
    }

    @GetMapping("/{accountNumber}")
    public Page<TransactionResponse> getTransactionHistory(
            @PathVariable String accountNumber,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @PageableDefault(
                    size = 10,
                    sort = "transactionDate",
                    direction = Sort.Direction.DESC
            ) Pageable pageable) {

    	LocalDateTime fromDateTime = null;
    	LocalDateTime toDateTime = null;

    	try {
    	    if (fromDate != null && !fromDate.isBlank()) {
    	        fromDateTime = LocalDate.parse(fromDate.trim()).atStartOfDay();
    	    }

    	    if (toDate != null && !toDate.isBlank()) {
    	        toDateTime = LocalDate.parse(toDate.trim()).atTime(23, 59, 59);
    	    }
    	} catch (java.time.format.DateTimeParseException ex) {
    	    throw new IllegalArgumentException(
    	            "Invalid date format. Use yyyy-MM-dd");
    	}

    	if (fromDateTime != null
    	        && toDateTime != null
    	        && fromDateTime.isAfter(toDateTime)) {
    	    throw new IllegalArgumentException(
    	            "From date cannot be after to date");
    	}

        return transactionService.getTransactionHistory(
                accountNumber,
                type,
                status,
                fromDateTime,
                toDateTime,
                pageable);
    }
    
    @GetMapping("/reference/{referenceNumber}")
    public TransactionResponse getTransactionByReference(
            @PathVariable String referenceNumber) {

        return transactionService.getTransactionByReference(referenceNumber);
    }
    
    @GetMapping("/summary/{accountNumber}")
    public TransactionSummaryResponse getTransactionSummary(
            @PathVariable String accountNumber) {

        return transactionService.getTransactionSummary(accountNumber);
    }
    @GetMapping("/amount-range")
    public ResponseEntity<List<BankTransaction>> getTransactionsByAmountRange(
            @RequestParam BigDecimal minAmount,
            @RequestParam BigDecimal maxAmount) {

        if (minAmount.compareTo(BigDecimal.ZERO) < 0
                || maxAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Amounts cannot be negative");
        }

        if (minAmount.compareTo(maxAmount) > 0) {
            throw new IllegalArgumentException(
                    "Minimum amount cannot exceed maximum amount");
        }

        List<BankTransaction> transactions =
                transactionService.getTransactionsByAmountRange(
                        minAmount, maxAmount);

        return ResponseEntity.ok(transactions);
    }
    
    
    
    @GetMapping("/type")
    public ResponseEntity<List<BankTransaction>> getTransactionsByType(
            @RequestParam String accountNumber,
            @RequestParam String transactionType) {
    	System.out.println(">>> GET /api/transactions/type controller called");

        if (accountNumber == null || accountNumber.isBlank()) {
            throw new IllegalArgumentException("Account number is required");
        }

        if (transactionType == null || transactionType.isBlank()) {
            throw new IllegalArgumentException("Transaction type is required");
        }

        transactionType = transactionType.trim().toUpperCase();

        if (!transactionType.equals("DEPOSIT")
                && !transactionType.equals("WITHDRAW")
                && !transactionType.equals("TRANSFER")) {
            throw new IllegalArgumentException(
                    "Allowed values: DEPOSIT, WITHDRAW, TRANSFER");
        }

        return ResponseEntity.ok(
                transactionService.getTransactionsByType(
                        accountNumber.trim(), transactionType));
    }
    
    @GetMapping("/status")
    public ResponseEntity<List<BankTransaction>> getTransactionsByStatus(
            @RequestParam String accountNumber,
            @RequestParam String status) {

        if (accountNumber == null || accountNumber.isBlank()) {
            throw new IllegalArgumentException("Account number is required");
        }

        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status is required");
        }

        return ResponseEntity.ok(
                transactionService.getTransactionsByStatus(
                        accountNumber.trim(),
                        status.trim().toUpperCase()));
    }
    
    @GetMapping("/filter")
    public ResponseEntity<List<BankTransaction>> getTransactionsByFilters(
            @RequestParam String accountNumber,
            @RequestParam String transactionType,
            @RequestParam String status) {

        if (accountNumber == null || accountNumber.isBlank()) {
            throw new IllegalArgumentException("Account number is required");
        }

        if (transactionType == null || transactionType.isBlank()) {
            throw new IllegalArgumentException("Transaction type is required");
        }

        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status is required");
        }

        transactionType = transactionType.trim().toUpperCase();
        status = status.trim().toUpperCase();
        accountNumber = accountNumber.trim();

        if (!transactionType.equals("DEPOSIT")
                && !transactionType.equals("WITHDRAW")
                && !transactionType.equals("TRANSFER")) {
            throw new IllegalArgumentException(
                    "Allowed transaction types: DEPOSIT, WITHDRAW, TRANSFER");
        }

        if (!status.equals("SUCCESS") && !status.equals("FAILED")) {
            throw new IllegalArgumentException(
                    "Allowed statuses: SUCCESS, FAILED");
        }

        return ResponseEntity.ok(
                transactionService.getTransactionsByFilters(
                        accountNumber, transactionType, status));
    }

}
