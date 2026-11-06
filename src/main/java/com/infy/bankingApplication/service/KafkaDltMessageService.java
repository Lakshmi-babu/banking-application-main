package com.infy.bankingApplication.service;

import java.time.Instant;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infy.bankingApplication.dto.KafkaDltMessageResponse;
import com.infy.bankingApplication.kafka.TransactionEvent;

@Service
public class KafkaDltMessageService {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private static final String DLT_TOPIC = "transaction-events.DLT";

    private final ObjectMapper objectMapper;

    public KafkaDltMessageService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<KafkaDltMessageResponse> getDltMessages(int limit) {

        Properties properties = new Properties();

        properties.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers);

        properties.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "dlt-monitor-" + UUID.randomUUID());

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
                "earliest");

        List<KafkaDltMessageResponse> messages = new ArrayList<>();

        try (KafkaConsumer<String, String> consumer =
                new KafkaConsumer<>(properties)) {

            List<TopicPartition> partitions =
                    consumer.partitionsFor(DLT_TOPIC)
                            .stream()
                            .map(info -> new TopicPartition(
                                    DLT_TOPIC,
                                    info.partition()))
                            .toList();

            if (partitions.isEmpty()) {
                return messages;
            }

            consumer.assign(partitions);
            consumer.seekToEnd(partitions);

            for (TopicPartition partition : partitions) {
                long endOffset = consumer.position(partition);
                consumer.seek(partition, Math.max(0, endOffset - limit));
            }

            long deadline = System.currentTimeMillis() + 3000;

            while (messages.size() < limit
                    && System.currentTimeMillis() < deadline) {

                ConsumerRecords<String, String> records =
                        consumer.poll(Duration.ofMillis(300));

                for (ConsumerRecord<String, String> record : records) {

                    try {
                        TransactionEvent event =
                                objectMapper.readValue(
                                        record.value(),
                                        TransactionEvent.class);

                        messages.add(new KafkaDltMessageResponse(
                                record.key(),
                                event.getTransactionReference(),
                                event.getUsername(),
                                event.getAccountNumber(),
                                event.getTransactionType(),
                                event.getAmount(),
                                event.getStatus(),
                                event.getMessage(),
                                record.partition(),
                                record.offset(),
                                record.timestamp() > 0
                                        ? Instant.ofEpochMilli(record.timestamp())
                                        : null));

                    } catch (Exception exception) {
                        messages.add(new KafkaDltMessageResponse(
                                record.key(),
                                null,
                                null,
                                null,
                                null,
                                null,
                                "UNREADABLE",
                                "Unable to deserialize DLT message",
                                record.partition(),
                                record.offset(),
                                record.timestamp() > 0
                                        ? Instant.ofEpochMilli(record.timestamp())
                                        : null));
                    }

                    if (messages.size() >= limit) {
                        break;
                    }
                }
            }
        }

        return messages;
    }
}
