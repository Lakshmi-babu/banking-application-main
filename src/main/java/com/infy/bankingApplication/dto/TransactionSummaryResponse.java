package com.infy.bankingApplication.dto;

import java.math.BigDecimal;

public class TransactionSummaryResponse {
	
	private String accountNumber;

    private long totalTransactions;
    private long successfulTransactions;
    private long failedTransactions;

    private long totalDeposits;
    private long totalWithdrawals;
    private long totalTransfers;

    private BigDecimal totalDepositAmount;
    private BigDecimal totalWithdrawalAmount;
    private BigDecimal totalTransferAmount;

    public TransactionSummaryResponse() {
    }

    public TransactionSummaryResponse(
            String accountNumber,
            long totalTransactions,
            long successfulTransactions,
            long failedTransactions,
            long totalDeposits,
            long totalWithdrawals,
            long totalTransfers,
            BigDecimal totalDepositAmount,
            BigDecimal totalWithdrawalAmount,
            BigDecimal totalTransferAmount) {

        this.accountNumber = accountNumber;
        this.totalTransactions = totalTransactions;
        this.successfulTransactions = successfulTransactions;
        this.failedTransactions = failedTransactions;
        this.totalDeposits = totalDeposits;
        this.totalWithdrawals = totalWithdrawals;
        this.totalTransfers = totalTransfers;
        this.totalDepositAmount = totalDepositAmount;
        this.totalWithdrawalAmount = totalWithdrawalAmount;
        this.totalTransferAmount = totalTransferAmount;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public long getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(long totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    public long getSuccessfulTransactions() {
        return successfulTransactions;
    }

    public void setSuccessfulTransactions(long successfulTransactions) {
        this.successfulTransactions = successfulTransactions;
    }

    public long getFailedTransactions() {
        return failedTransactions;
    }

    public void setFailedTransactions(long failedTransactions) {
        this.failedTransactions = failedTransactions;
    }

    public long getTotalDeposits() {
        return totalDeposits;
    }

    public void setTotalDeposits(long totalDeposits) {
        this.totalDeposits = totalDeposits;
    }

    public long getTotalWithdrawals() {
        return totalWithdrawals;
    }

    public void setTotalWithdrawals(long totalWithdrawals) {
        this.totalWithdrawals = totalWithdrawals;
    }

    public long getTotalTransfers() {
        return totalTransfers;
    }

    public void setTotalTransfers(long totalTransfers) {
        this.totalTransfers = totalTransfers;
    }

    public BigDecimal getTotalDepositAmount() {
        return totalDepositAmount;
    }

    public void setTotalDepositAmount(BigDecimal totalDepositAmount) {
        this.totalDepositAmount = totalDepositAmount;
    }

    public BigDecimal getTotalWithdrawalAmount() {
        return totalWithdrawalAmount;
    }

    public void setTotalWithdrawalAmount(BigDecimal totalWithdrawalAmount) {
        this.totalWithdrawalAmount = totalWithdrawalAmount;
    }

    public BigDecimal getTotalTransferAmount() {
        return totalTransferAmount;
    }

    public void setTotalTransferAmount(BigDecimal totalTransferAmount) {
        this.totalTransferAmount = totalTransferAmount;
    }

}
