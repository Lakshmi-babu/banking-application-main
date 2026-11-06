package com.infy.bankingApplication.dto;

import java.time.LocalDateTime;

public class KafkaDltSummaryResponse {

    private String topic;
    private int partitionCount;
    private long totalMessages;
    private long earliestOffset;
    private long latestOffset;
    private LocalDateTime checkedAt;

    public KafkaDltSummaryResponse(
            String topic,
            int partitionCount,
            long totalMessages,
            long earliestOffset,
            long latestOffset,
            LocalDateTime checkedAt) {

        this.topic = topic;
        this.partitionCount = partitionCount;
        this.totalMessages = totalMessages;
        this.earliestOffset = earliestOffset;
        this.latestOffset = latestOffset;
        this.checkedAt = checkedAt;
    }

    public String getTopic() {
        return topic;
    }

    public int getPartitionCount() {
        return partitionCount;
    }

    public long getTotalMessages() {
        return totalMessages;
    }

    public long getEarliestOffset() {
        return earliestOffset;
    }

    public long getLatestOffset() {
        return latestOffset;
    }

    public LocalDateTime getCheckedAt() {
        return checkedAt;
    }
}
