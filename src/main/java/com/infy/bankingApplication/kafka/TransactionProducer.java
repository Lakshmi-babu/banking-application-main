package com.infy.bankingApplication.kafka;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.infy.bankingApplication.entity.BankTransaction;

@Service
public class TransactionProducer {
	
	private static final String TOPIC = "transaction-events";

    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;

    public TransactionProducer(
            KafkaTemplate<String, TransactionEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendTransaction(BankTransaction transaction) {

        TransactionEvent event = new TransactionEvent();

        event.setTransactionReference(
                transaction.getReferenceNumber()
        );

        // For DEPOSIT → destination account
        // For WITHDRAW → source account
        // For TRANSFER → source account
        String accountNumber;

        if ("DEPOSIT".equals(transaction.getTransactionType())) {
            accountNumber = transaction.getDestinationAccount();
        } else {
            accountNumber = transaction.getSourceAccount();
        }

        event.setAccountNumber(accountNumber);

        event.setTransactionType(
                transaction.getTransactionType()
        );

        event.setAmount(
                transaction.getAmount().doubleValue()
        );

        event.setStatus(
                transaction.getStatus()
        );

        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        event.setUsername(username);

        event.setMessage(
                transaction.getTransactionType()
                        + " transaction completed with status "
                        + transaction.getStatus()
        );

        kafkaTemplate.send(
                TOPIC,
                transaction.getReferenceNumber(),
                event
        );

        System.out.println(
                "Kafka TransactionEvent sent: "
                + transaction.getReferenceNumber()
        );
    }


}
