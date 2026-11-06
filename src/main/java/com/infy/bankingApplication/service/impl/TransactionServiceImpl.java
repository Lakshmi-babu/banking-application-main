package com.infy.bankingApplication.service.impl;



import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.infy.bankingApplication.dto.TransactionResponse;
import com.infy.bankingApplication.dto.TransactionSummaryResponse;
import com.infy.bankingApplication.entity.Account;
import com.infy.bankingApplication.entity.BankTransaction;
import com.infy.bankingApplication.entity.User;
import com.infy.bankingApplication.exception.ResourceNotFoundException;
import com.infy.bankingApplication.repository.AccountRepository;
import com.infy.bankingApplication.repository.BankTransactionRepository;
import com.infy.bankingApplication.repository.UserRepository;
import com.infy.bankingApplication.service.TransactionService;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final BankTransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public TransactionServiceImpl(
            BankTransactionRepository transactionRepository, AccountRepository accountRepository,UserRepository userRepository) {

        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        
    }

    @Override
    public Page<TransactionResponse> getTransactionHistory(
            String accountNumber,
            String type,
            String status,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable) {

        accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Account not found"));

        // Validate transaction type
     // Normalize and validate transaction type
        if (type != null) {
            type = type.trim().toUpperCase();

            if (type.isBlank()) {
                type = null;
            } else if (!type.equals("DEPOSIT")
                    && !type.equals("WITHDRAW")
                    && !type.equals("TRANSFER")) {

                throw new IllegalArgumentException(
                        "Invalid transaction type. Allowed values: DEPOSIT, WITHDRAW, TRANSFER");
            }
        }

        // Normalize and validate transaction status
        if (status != null) {
            status = status.trim().toUpperCase();

            if (status.isBlank()) {
                status = null;
            } else if (!status.equals("SUCCESS")
                    && !status.equals("FAILED")) {

                throw new IllegalArgumentException(
                        "Invalid transaction status. Allowed values: SUCCESS, FAILED");
            }
        }

        Page<BankTransaction> transactions =
                transactionRepository.findTransactionHistory(
                        accountNumber,
                        type,
                        status,
                        fromDate,
                        toDate,
                        pageable);

        return transactions.map(this::convertToResponse);
    }
    
    @Override
    public TransactionResponse getTransactionByReference(String referenceNumber) {

        BankTransaction transaction =
                transactionRepository.findByReferenceNumber(referenceNumber)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Transaction not found with reference number: "
                                                + referenceNumber));

        return convertToResponse(transaction);
    }
    
    @Override
    public TransactionSummaryResponse getTransactionSummary(
            String accountNumber) {

        // Check whether account exists
        accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Account not found"));

        // Transaction counts
        long totalTransactions =
                transactionRepository
                        .countBySourceAccountOrDestinationAccount(
                                accountNumber,
                                accountNumber);

        long successfulTransactions =
                transactionRepository
                        .countByAccountAndStatus(
                                accountNumber,
                                "SUCCESS");

        long failedTransactions =
                transactionRepository
                        .countByAccountAndStatus(
                                accountNumber,
                                "FAILED");

        long totalDeposits =
                transactionRepository
                        .countByAccountAndTransactionType(
                                accountNumber,
                                "DEPOSIT");

        long totalWithdrawals =
                transactionRepository
                        .countByAccountAndTransactionType(
                                accountNumber,
                                "WITHDRAW");

        long totalTransfers =
                transactionRepository
                        .countByAccountAndTransactionType(
                                accountNumber,
                                "TRANSFER");

        // Transaction amounts
        BigDecimal totalDepositAmount =
                transactionRepository
                        .sumAmountByAccountAndTransactionType(
                                accountNumber,
                                "DEPOSIT");

        BigDecimal totalWithdrawalAmount =
                transactionRepository
                        .sumAmountByAccountAndTransactionType(
                                accountNumber,
                                "WITHDRAW");

        BigDecimal totalTransferAmount =
                transactionRepository
                        .sumAmountByAccountAndTransactionType(
                                accountNumber,
                                "TRANSFER");

        return new TransactionSummaryResponse(
                accountNumber,
                totalTransactions,
                successfulTransactions,
                failedTransactions,
                totalDeposits,
                totalWithdrawals,
                totalTransfers,
                totalDepositAmount,
                totalWithdrawalAmount,
                totalTransferAmount);
    }

    private TransactionResponse convertToResponse(
            BankTransaction transaction) {

        return new TransactionResponse(
                transaction.getId(),
                transaction.getReferenceNumber(),
                transaction.getTransactionType(),
                transaction.getSourceAccount(),
                transaction.getDestinationAccount(),
                transaction.getAmount(),
                transaction.getTransactionDate(),
                transaction.getStatus()
        );
    }
    @Override
    public List<BankTransaction> getTransactionsByAmountRange(
            BigDecimal minAmount,
            BigDecimal maxAmount) {

        return transactionRepository
                .findByAmountBetweenOrderByAmountAsc(
                        minAmount, maxAmount);
    }
    
    @Override
    public List<BankTransaction> getTransactionsByType(
            String accountNumber, String transactionType) {

    	String username = SecurityContextHolder.getContext()
    	        .getAuthentication().getName();

    	Account account = accountRepository.findByAccountNumber(accountNumber)
    	        .orElseThrow(() ->
    	                new ResourceNotFoundException("Account not found"));

    	// Find the authenticated user
    	User user = userRepository.findByUsername(username)
    	        .orElseThrow(() ->
    	                new ResourceNotFoundException("Account not found"));

    	// Verify account ownership
    	if (account.getCustomer() == null
    	        || !account.getCustomer().getId().equals(user.getId())) {
    	    throw new ResourceNotFoundException("Account not found");
    	}

    	// Fetch transactions by type
    	return transactionRepository.findByAccountAndTransactionType(
    	        accountNumber, transactionType);
}
    @Override
    public List<BankTransaction> getTransactionsByStatus(
            String accountNumber, String status) {

        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Account not found"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Account not found"));

        if (account.getCustomer() == null
                || !account.getCustomer().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Account not found");
        }

        if (!status.equals("SUCCESS") && !status.equals("FAILED")) {
            throw new IllegalArgumentException(
                    "Invalid status. Allowed values: SUCCESS, FAILED");
        }

        return transactionRepository.findByAccountAndStatus(
                accountNumber, status);
    }
    
    @Override
    public List<BankTransaction> getTransactionsByFilters(
            String accountNumber,
            String transactionType,
            String status) {

        // Verify that the account exists
        accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Account not found"));

        return transactionRepository.findByAccountAndTypeAndStatus(
                accountNumber,
                transactionType,
                status);
    }
}
