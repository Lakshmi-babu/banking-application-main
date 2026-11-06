package com.infy.bankingApplication.dto;

import java.time.Instant;

public class KafkaDltMessageResponse {

    private String messageKey;
    private String transactionReference;
    private String username;
    private String accountNumber;
    private String transactionType;
    private Double amount;
    private String status;
    private String message;
    private int partition;
    private long offset;
    private Instant timestamp;

    public KafkaDltMessageResponse(
            String messageKey,
            String transactionReference,
            String username,
            String accountNumber,
            String transactionType,
            Double amount,
            String status,
            String message,
            int partition,
            long offset,
            Instant timestamp) {

        this.messageKey = messageKey;
        this.transactionReference = transactionReference;
        this.username = username;
        this.accountNumber = accountNumber;
        this.transactionType = transactionType;
        this.amount = amount;
        this.status = status;
        this.message = message;
        this.partition = partition;
        this.offset = offset;
        this.timestamp = timestamp;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public String getUsername() {
        return username;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public Double getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public int getPartition() {
        return partition;
    }

    public long getOffset() {
        return offset;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
