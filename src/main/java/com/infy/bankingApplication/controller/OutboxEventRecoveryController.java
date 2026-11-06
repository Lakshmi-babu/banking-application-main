package com.infy.bankingApplication.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.infy.bankingApplication.dto.OutboxEventResponse;
import com.infy.bankingApplication.dto.OutboxSummaryResponse;
import com.infy.bankingApplication.entity.OutboxEvent;
import com.infy.bankingApplication.service.OutboxEventRecoveryService;

@RestController
@RequestMapping("/api/admin/outbox-events")
public class OutboxEventRecoveryController {
	
	private final OutboxEventRecoveryService recoveryService;

    public OutboxEventRecoveryController(
            OutboxEventRecoveryService recoveryService) {
        this.recoveryService = recoveryService;
    }

    @PostMapping("/{eventId}/retry")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OutboxEventResponse> retryFailedEvent(
            @PathVariable Long eventId) {

        OutboxEvent event = recoveryService.retryFailedEvent(eventId);

        OutboxEventResponse response = new OutboxEventResponse(
                event.getId(),
                event.getAggregateId(),
                event.getAggregateType(),
                event.getEventType(),
                event.getStatus(),
                event.getRetryCount(),
                event.getLastError(),
                event.getCreatedAt(),
                event.getPublishedAt()
        );

        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OutboxEventResponse>> getOutboxEvents() {

        List<OutboxEventResponse> events = recoveryService.getAllEvents();

        return ResponseEntity.ok(events);
    }
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OutboxEventResponse>> getOutboxEventsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page must be >= 0 and size must be between 1 and 100");
        }

        Page<OutboxEventResponse> events =
                recoveryService.getOutboxEventsPage(page, size);

        return ResponseEntity.ok(events);
    }
    
    @GetMapping("/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OutboxEventResponse>> getOutboxEventsByStatus(
            @RequestParam String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page must be >= 0 and size must be between 1 and 100");
        }

        Page<OutboxEventResponse> events =
                recoveryService.getOutboxEventsByStatus(status, page, size);

        return ResponseEntity.ok(events);
    }
    
    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OutboxSummaryResponse> getOutboxSummary() {

        OutboxSummaryResponse summary =
                recoveryService.getOutboxSummary();

        return ResponseEntity.ok(summary);
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OutboxEventResponse>> searchOutboxEvents(
            @RequestParam String reference) {

        List<OutboxEventResponse> events =
                recoveryService.searchByTransactionReference(reference);

        return ResponseEntity.ok(events);
    }
    
    
    

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(
            IllegalArgumentException ex) {

        HttpStatus status = ex.getMessage() != null
                && ex.getMessage().startsWith("Outbox event not found:")
                ? HttpStatus.NOT_FOUND
                : HttpStatus.BAD_REQUEST;

        return ResponseEntity.status(status).body(ex.getMessage());
    }
    
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleInvalidStatus(IllegalStateException ex) {
        return ex.getMessage();
    }

}
