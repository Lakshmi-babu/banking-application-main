package com.infy.bankingApplication.service;

import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infy.bankingApplication.dto.KafkaDltRetryResponse;
import com.infy.bankingApplication.kafka.TransactionEvent;

@Service
public class KafkaDltRetryService {
	
	private static final String DLT_TOPIC =
            "transaction-events.DLT";

    private static final String ORIGINAL_TOPIC =
            "transaction-events";

    private static final Logger log =
            LoggerFactory.getLogger(KafkaDltRetryService.class);

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;

    private final ObjectMapper objectMapper;

    private final AuditLogService auditLogService;

    public KafkaDltRetryService(
            KafkaTemplate<String, TransactionEvent> kafkaTemplate,
            ObjectMapper objectMapper,
            AuditLogService auditLogService) {

        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.auditLogService = auditLogService;
    }

    public KafkaDltRetryResponse retryMessage(
            int partition,
            long offset) {

        if (partition < 0 || offset < 0) {
            throw new IllegalArgumentException(
                    "Partition and offset must be non-negative");
        }

        String transactionReference = null;

        try {

            Properties properties = new Properties();

            properties.put(
                    ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                    bootstrapServers);

            properties.put(
                    ConsumerConfig.GROUP_ID_CONFIG,
                    "dlt-retry-" + UUID.randomUUID());

            properties.put(
                    ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                    StringDeserializer.class.getName());

            properties.put(
                    ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                    StringDeserializer.class.getName());

            properties.put(
                    ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                    false);

            properties.put(
                    ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                    "none");

            try (KafkaConsumer<String, String> consumer =
                    new KafkaConsumer<>(properties)) {

                TopicPartition topicPartition =
                        new TopicPartition(DLT_TOPIC, partition);

                consumer.assign(List.of(topicPartition));

                consumer.seek(topicPartition, offset);

                ConsumerRecord<String, String> record = null;

                long deadline =
                        System.currentTimeMillis() + 5000;

                while (System.currentTimeMillis() < deadline) {

                    var records =
                            consumer.poll(Duration.ofMillis(300));

                    for (ConsumerRecord<String, String> current : records) {

                        if (current.offset() == offset) {
                            record = current;
                            break;
                        }
                    }

                    if (record != null) {
                        break;
                    }

                    if (consumer.position(topicPartition) > offset) {
                        break;
                    }
                }

                if (record == null) {
                    throw new IllegalArgumentException(
                            "DLT message not found for the supplied partition and offset");
                }

                TransactionEvent event =
                        objectMapper.readValue(
                                record.value(),
                                TransactionEvent.class);

                transactionReference =
                        event.getTransactionReference();

                kafkaTemplate.send(
                        ORIGINAL_TOPIC,
                        record.key(),
                        event
                ).get(10, TimeUnit.SECONDS);

                recordAuditSafely(
                        partition,
                        offset,
                        transactionReference,
                        "SUCCESS");

                return new KafkaDltRetryResponse(
                        "PUBLISHED",
                        "DLT message successfully replayed to original topic",
                        partition,
                        offset,
                        transactionReference);
            }

        } catch (IllegalArgumentException exception) {

            recordAuditSafely(
                    partition,
                    offset,
                    transactionReference,
                    "FAILED");

            throw exception;

        } catch (Exception exception) {

            recordAuditSafely(
                    partition,
                    offset,
                    transactionReference,
                    "FAILED");

            throw new RuntimeException(
                    "Failed to replay DLT message: "
                            + exception.getMessage(),
                    exception);
        }
    }

    private void recordAuditSafely(
            int partition,
            long offset,
            String transactionReference,
            String status) {

        try {

            auditLogService.logDltReplay(
                    partition,
                    offset,
                    transactionReference,
                    status);

        } catch (Exception exception) {

            log.error(
                    "Failed to save DLT replay audit record",
                    exception);
        }
    }

}
