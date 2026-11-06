package com.infy.bankingApplication.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.infy.bankingApplication.entity.OutboxEvent;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    List<OutboxEvent> findByStatusOrderByCreatedAtAsc(String status);
    
    Page<OutboxEvent> findAllBy(Pageable pageable);
    
    Page<OutboxEvent> findByStatus(
            String status,
            Pageable pageable);
    
    // Count outbox events by status
    long countByStatus(String status);
    
    List<OutboxEvent> findByAggregateId(String aggregateId);
    
    long deleteByStatusAndPublishedAtBefore(
            String status,
            LocalDateTime cutoffDate);

}
