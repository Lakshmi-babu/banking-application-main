package com.infy.bankingApplication.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.TopicDescription;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.infy.bankingApplication.dto.KafkaDltHealthResponse;

@Service
public class KafkaDltHealthService {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private static final String DLT_TOPIC = "transaction-events.DLT";

    public KafkaDltHealthResponse checkDltHealth() {

        try (AdminClient adminClient = AdminClient.create(
                Map.of(
                    AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,
                    bootstrapServers))) {

            TopicDescription topicDescription = adminClient
                    .describeTopics(java.util.List.of(DLT_TOPIC))
                    .allTopicNames()
                    .get(5, TimeUnit.SECONDS)
                    .get(DLT_TOPIC);

            int partitionCount =
                    topicDescription.partitions().size();

            return new KafkaDltHealthResponse(
                    true,
                    bootstrapServers,
                    DLT_TOPIC,
                    true,
                    partitionCount,
                    LocalDateTime.now(),
                    "Kafka broker is reachable and DLT topic is available.");

        } catch (Exception exception) {

            String errorMessage = exception.getMessage();

            if (errorMessage == null || errorMessage.isBlank()) {
                errorMessage = exception.getClass().getSimpleName();
            }

            return new KafkaDltHealthResponse(
                    false,
                    bootstrapServers,
                    DLT_TOPIC,
                    false,
                    0,
                    LocalDateTime.now(),
                    "Kafka DLT health check failed: " + errorMessage);
        }
    }
}
