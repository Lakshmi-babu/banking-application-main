package com.infy.bankingApplication;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;

import com.infy.bankingApplication.kafka.TransactionEvent;

import static org.mockito.Mockito.mock;

@TestConfiguration(proxyBeanMethods = false)
public class KafkaTestConfiguration {

    @Bean
    @SuppressWarnings("unchecked")
    public KafkaTemplate<String, TransactionEvent> kafkaTemplate() {
        return mock(KafkaTemplate.class);
    }
}
