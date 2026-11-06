package com.infy.bankingApplication.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.infy.bankingApplication.dto.OutboxEventResponse;
import com.infy.bankingApplication.dto.OutboxSummaryResponse;
import com.infy.bankingApplication.entity.OutboxEvent;
import com.infy.bankingApplication.repository.OutboxEventRepository;

@Service
public class OutboxEventRecoveryService {
	
	private final OutboxEventRepository outboxEventRepository;

    public OutboxEventRecoveryService(
            OutboxEventRepository outboxEventRepository) {
        this.outboxEventRepository = outboxEventRepository;
    }

    @Transactional
    public OutboxEvent retryFailedEvent(Long eventId) {

        OutboxEvent event = outboxEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Outbox event not found: " + eventId));

        if (!"FAILED".equals(event.getStatus())) {
            throw new IllegalStateException(
                    "Only FAILED events can be retried. Current status: "
                            + event.getStatus());
        }

        event.setStatus("PENDING");
        event.setRetryCount(0);
        event.setLastError(null);

        return outboxEventRepository.save(event);
    }
    @Transactional(readOnly = true)
    public List<OutboxEventResponse> getAllEvents() {

        return outboxEventRepository.findAll()
                .stream()
                .map(event -> new OutboxEventResponse(
                        event.getId(),
                        event.getAggregateId(),
                        event.getAggregateType(),
                        event.getEventType(),
                        event.getStatus(),
                        event.getRetryCount(),
                        event.getLastError(),
                        event.getCreatedAt(),
                        event.getPublishedAt()))
                .toList();
    }
    
    @Transactional(readOnly = true)
    public Page<OutboxEventResponse> getOutboxEventsPage(
            int page, int size) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        return outboxEventRepository.findAllBy(pageable)
                .map(event -> new OutboxEventResponse(
                        event.getId(),
                        event.getAggregateId(),
                        event.getAggregateType(),
                        event.getEventType(),
                        event.getStatus(),
                        event.getRetryCount(),
                        event.getLastError(),
                        event.getCreatedAt(),
                        event.getPublishedAt()));
    }
    
    @Transactional(readOnly = true)
    public Page<OutboxEventResponse> getOutboxEventsByStatus(
            String status, int page, int size) {

        String normalizedStatus = status.trim().toUpperCase();

        if (!normalizedStatus.equals("PENDING")
                && !normalizedStatus.equals("PUBLISHED")
                && !normalizedStatus.equals("FAILED")) {
            throw new IllegalArgumentException(
                    "Invalid status. Allowed values: PENDING, PUBLISHED, FAILED");
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        return outboxEventRepository.findByStatus(normalizedStatus, pageable)
                .map(event -> new OutboxEventResponse(
                        event.getId(),
                        event.getAggregateId(),
                        event.getAggregateType(),
                        event.getEventType(),
                        event.getStatus(),
                        event.getRetryCount(),
                        event.getLastError(),
                        event.getCreatedAt(),
                        event.getPublishedAt()));
    }
    
    @Transactional(readOnly = true)
    public OutboxSummaryResponse getOutboxSummary() {

        // Total number of outbox events
        long totalEvents = outboxEventRepository.count();

        // Count events based on status
        long pendingEvents =
                outboxEventRepository.countByStatus("PENDING");

        long publishedEvents =
                outboxEventRepository.countByStatus("PUBLISHED");

        long failedEvents =
                outboxEventRepository.countByStatus("FAILED");

        // Return summary response
        return new OutboxSummaryResponse(
                totalEvents,
                pendingEvents,
                publishedEvents,
                failedEvents
        );
    }
    
    @Transactional(readOnly = true)
    public List<OutboxEventResponse> searchByTransactionReference(
            String reference) {

        if (reference == null || reference.isBlank()) {
            throw new IllegalArgumentException(
                    "Transaction reference must not be empty");
        }

        List<OutboxEvent> events =
                outboxEventRepository.findByAggregateId(reference.trim());

        return events.stream()
                .map(event -> new OutboxEventResponse(
                        event.getId(),
                        event.getAggregateId(),
                        event.getAggregateType(),
                        event.getEventType(),
                        event.getStatus(),
                        event.getRetryCount(),
                        event.getLastError(),
                        event.getCreatedAt(),
                        event.getPublishedAt()
                ))
                .toList();
    }

}
