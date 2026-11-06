package com.infy.bankingApplication.dto;

import java.time.LocalDateTime;

public class KafkaDltHealthResponse {

    private boolean healthy;
    private String broker;
    private String topic;
    private boolean topicExists;
    private int partitionCount;
    private LocalDateTime checkedAt;
    private String message;

    public KafkaDltHealthResponse(
            boolean healthy,
            String broker,
            String topic,
            boolean topicExists,
            int partitionCount,
            LocalDateTime checkedAt,
            String message) {

        this.healthy = healthy;
        this.broker = broker;
        this.topic = topic;
        this.topicExists = topicExists;
        this.partitionCount = partitionCount;
        this.checkedAt = checkedAt;
        this.message = message;
    }

    public boolean isHealthy() {
        return healthy;
    }

    public String getBroker() {
        return broker;
    }

    public String getTopic() {
        return topic;
    }

    public boolean isTopicExists() {
        return topicExists;
    }

    public int getPartitionCount() {
        return partitionCount;
    }

    public LocalDateTime getCheckedAt() {
        return checkedAt;
    }

    public String getMessage() {
        return message;
    }
}
