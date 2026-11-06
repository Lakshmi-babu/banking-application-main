package com.infy.bankingApplication.dto;

import java.time.LocalDateTime;

public class OutboxEventResponse {
	
	private Long id;
    private String aggregateId;
    private String aggregateType;
    private String eventType;
    private String status;
    private int retryCount;
    private String lastError;
    private LocalDateTime createdAt;
    private LocalDateTime publishedAt;

    public OutboxEventResponse() {
    }

    public OutboxEventResponse(
            Long id,
            String aggregateId,
            String aggregateType,
            String eventType,
            String status,
            int retryCount,
            String lastError,
            LocalDateTime createdAt,
            LocalDateTime publishedAt) {

        this.id = id;
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.eventType = eventType;
        this.status = status;
        this.retryCount = retryCount;
        this.lastError = lastError;
        this.createdAt = createdAt;
        this.publishedAt = publishedAt;
    }

    public Long getId() {
        return id;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public String getEventType() {
        return eventType;
    }

    public String getStatus() {
        return status;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public String getLastError() {
        return lastError;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

}
