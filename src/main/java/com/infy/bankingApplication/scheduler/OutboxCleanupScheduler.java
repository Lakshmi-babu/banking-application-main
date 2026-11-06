package com.infy.bankingApplication.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.infy.bankingApplication.service.OutboxCleanupService;

@ConditionalOnProperty(
        name = "app.scheduling.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Component
public class OutboxCleanupScheduler {
	
	private static final Logger log =
            LoggerFactory.getLogger(OutboxCleanupScheduler.class);

    private final OutboxCleanupService outboxCleanupService;

    @Value("${outbox.cleanup.retention-days:30}")
    private long retentionDays;

    public OutboxCleanupScheduler(
            OutboxCleanupService outboxCleanupService) {

        this.outboxCleanupService = outboxCleanupService;
    }

    @Scheduled(
            fixedDelayString = "${outbox.cleanup.fixed-delay:86400000}",
            initialDelay = 60000
    )
    public void cleanupOldOutboxEvents() {

        log.info(
                "Starting outbox cleanup. Retention period: {} days",
                retentionDays
        );

        try {

            long deletedCount =
                    outboxCleanupService.cleanupPublishedEvents();

            log.info(
                    "Outbox cleanup scheduler completed. Deleted {} events.",
                    deletedCount
            );

        } catch (Exception e) {

            log.error(
                    "Outbox cleanup scheduler failed",
                    e
            );
        }
    }

}
