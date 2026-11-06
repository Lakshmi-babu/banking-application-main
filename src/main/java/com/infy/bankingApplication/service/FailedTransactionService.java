package com.infy.bankingApplication.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infy.bankingApplication.entity.BankTransaction;
import com.infy.bankingApplication.entity.OutboxEvent;
import com.infy.bankingApplication.kafka.TransactionEvent;
import com.infy.bankingApplication.repository.BankTransactionRepository;
import com.infy.bankingApplication.repository.OutboxEventRepository;

@Service
public class FailedTransactionService {
	
	private final BankTransactionRepository transactionRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public FailedTransactionService(
            BankTransactionRepository transactionRepository,
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper) {

        this.transactionRepository = transactionRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BankTransaction saveFailedTransaction(
            String type,
            String source,
            String destination,
            BigDecimal amount,
            String username) {

        BankTransaction transaction = new BankTransaction();

        transaction.setReferenceNumber(generateReferenceNumber());
        transaction.setTransactionType(type);
        transaction.setSourceAccount(source);
        transaction.setDestinationAccount(destination);
        transaction.setAmount(amount);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setStatus("FAILED");

        BankTransaction savedTransaction =
                transactionRepository.save(transaction);

        saveFailedOutboxEvent(savedTransaction,username);

        return savedTransaction;
    }

    private void saveFailedOutboxEvent(
            BankTransaction transaction,String username) {

        TransactionEvent event = new TransactionEvent();

        event.setTransactionReference(
                transaction.getReferenceNumber());

        event.setAccountNumber(
                transaction.getSourceAccount());

        event.setTransactionType(
                transaction.getTransactionType());

        event.setAmount(
                transaction.getAmount().doubleValue());

        event.setStatus(transaction.getStatus());

        event.setUsername(username);

        event.setMessage(
                transaction.getTransactionType()
                        + " transaction completed with status FAILED");

        try {
            OutboxEvent outboxEvent = new OutboxEvent();

            outboxEvent.setAggregateType("BankTransaction");
            outboxEvent.setEventType(
                    transaction.getTransactionType());
            outboxEvent.setAggregateId(
                    transaction.getReferenceNumber());
            outboxEvent.setPayload(
                    objectMapper.writeValueAsString(event));
            outboxEvent.setStatus("PENDING");
            outboxEvent.setCreatedAt(LocalDateTime.now());

            outboxEventRepository.save(outboxEvent);

        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Failed to serialize failed transaction event", e);
        }
    }

    private String generateReferenceNumber() {

        long number = ThreadLocalRandom.current()
                .nextLong(100000000L, 999999999L);

        return "TXN-" + number;
    }

}
