package com.infy.bankingApplication.kafka;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infy.bankingApplication.entity.OutboxEvent;
import com.infy.bankingApplication.repository.OutboxEventRepository;



@ConditionalOnProperty(
        name = "app.scheduling.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Component
public class OutboxEventPublisher {
	
	private static final String TOPIC = "transaction-events";
	private static final int MAX_RETRIES = 5;

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OutboxEventPublisher(
            OutboxEventRepository outboxEventRepository,
            KafkaTemplate<String, TransactionEvent> kafkaTemplate,
            ObjectMapper objectMapper) {

        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 5000)
    public void publishPendingEvents() {

        List<OutboxEvent> pendingEvents =
                outboxEventRepository
                        .findByStatusOrderByCreatedAtAsc("PENDING");

        for (OutboxEvent outboxEvent : pendingEvents) {
            try {
                TransactionEvent event = objectMapper.readValue(
                        outboxEvent.getPayload(),
                        TransactionEvent.class
                );

                kafkaTemplate.send(
                        TOPIC,
                        event.getTransactionReference(),
                        event
                ).get(10, TimeUnit.SECONDS);

                outboxEvent.setStatus("PUBLISHED");
                outboxEvent.setPublishedAt(LocalDateTime.now());

                outboxEventRepository.save(outboxEvent);

                System.out.println(
                        "Outbox event published successfully: "
                                + outboxEvent.getId()
                );

            } catch (Exception e) {

                // Step 1: Increase retry count
                int retryCount = outboxEvent.getRetryCount() + 1;
                outboxEvent.setRetryCount(retryCount);

                // Step 2: Mark event FAILED after 5 attempts
                if (retryCount >= MAX_RETRIES) {
                    outboxEvent.setStatus("FAILED");
                }

                // Step 3: Capture error message
                String errorMessage = e.getMessage();

                if (errorMessage == null || errorMessage.isBlank()) {
                    errorMessage = e.getClass().getSimpleName();
                }

                if (errorMessage.length() > 1000) {
                    errorMessage = errorMessage.substring(0, 1000);
                }

                outboxEvent.setLastError(errorMessage);

                // Step 4: Save failure details
                try {
                    outboxEventRepository.saveAndFlush(outboxEvent);
                } catch (Exception saveException) {

                    System.err.println(
                            "Could not save retry details for outbox event "
                                    + outboxEvent.getId()
                    );

                    saveException.printStackTrace();
                }

                // Step 5: Log failure
                System.err.println(
                        "Failed to publish outbox event "
                                + outboxEvent.getId()
                                + ". Retry count: "
                                + outboxEvent.getRetryCount()
                                + ". Status: "
                                + outboxEvent.getStatus()
                );

                e.printStackTrace();
            }
        }
    }
	
	

}
