package com.infy.bankingApplication.service;



import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.infy.bankingApplication.dto.TransactionResponse;
import com.infy.bankingApplication.dto.TransactionSummaryResponse;
import com.infy.bankingApplication.entity.BankTransaction;



public interface TransactionService {
	
	Page<TransactionResponse> getTransactionHistory(
            String accountNumber, String type,String status,LocalDateTime fromDate,
            LocalDateTime toDate, Pageable pageable);
	
	TransactionResponse getTransactionByReference(String referenceNumber);
	
	TransactionSummaryResponse getTransactionSummary(String accountNumber);
	
	List<BankTransaction> getTransactionsByAmountRange(
	        BigDecimal minAmount,
	        BigDecimal maxAmount);
	
	List<BankTransaction> getTransactionsByType(
	        String accountNumber, String transactionType);
	
	List<BankTransaction> getTransactionsByStatus(
	        String accountNumber, String status);
	
	List<BankTransaction> getTransactionsByFilters(
	        String accountNumber,
	        String transactionType,
	        String status);
	
	
}
