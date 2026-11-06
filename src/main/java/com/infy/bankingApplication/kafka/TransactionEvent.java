package com.infy.bankingApplication.kafka;

public class TransactionEvent {
	
	private String transactionReference;
    private String username;
    private String accountNumber;
    private String transactionType;
    private Double amount;
    private String status;
    private String message;

    public TransactionEvent() {
    }

    public TransactionEvent(
            String transactionReference,
            String username,
            String accountNumber,
            String transactionType,
            Double amount,
            String status,
            String message) {

        this.transactionReference = transactionReference;
        this.username = username;
        this.accountNumber = accountNumber;
        this.transactionType = transactionType;
        this.amount = amount;
        this.status = status;
        this.message = message;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


}
