package com.infy.bankingApplication.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.transaction.annotation.Transactional;

import com.infy.bankingApplication.repository.OutboxEventRepository;

@Service
public class OutboxCleanupService {
	
	private static final Logger log =
            LoggerFactory.getLogger(OutboxCleanupService.class);

    private final OutboxEventRepository outboxEventRepository;

    @Value("${outbox.cleanup.retention-days:30}")
    private long retentionDays;

    public OutboxCleanupService(
            OutboxEventRepository outboxEventRepository) {

        this.outboxEventRepository = outboxEventRepository;
    }

    @Transactional
    public long cleanupPublishedEvents() {

        // Calculate retention cutoff date
        LocalDateTime cutoffDate =
                LocalDateTime.now().minusDays(retentionDays);

        // Delete only old PUBLISHED events
        long deletedCount =
                outboxEventRepository
                        .deleteByStatusAndPublishedAtBefore(
                                "PUBLISHED",
                                cutoffDate
                        );

        log.info(
                "Outbox cleanup completed. Deleted {} published events older than {} days.",
                deletedCount,
                retentionDays
        );

        return deletedCount;
    }

}
