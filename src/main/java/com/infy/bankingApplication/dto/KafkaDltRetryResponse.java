package com.infy.bankingApplication.dto;

public class KafkaDltRetryResponse {
	
	private String status;
    private String message;
    private int partition;
    private long offset;
    private String transactionReference;

    public KafkaDltRetryResponse(
            String status,
            String message,
            int partition,
            long offset,
            String transactionReference) {

        this.status = status;
        this.message = message;
        this.partition = partition;
        this.offset = offset;
        this.transactionReference = transactionReference;
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

    public String getTransactionReference() {
        return transactionReference;
    }

}
