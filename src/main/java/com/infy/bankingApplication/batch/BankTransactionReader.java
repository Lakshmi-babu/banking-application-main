package com.infy.bankingApplication.batch;

import org.springframework.batch.infrastructure.item.database.JpaPagingItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.infy.bankingApplication.entity.BankTransaction;

import jakarta.persistence.EntityManagerFactory;


@Configuration
public class BankTransactionReader {
	
	@Bean
    public JpaPagingItemReader<BankTransaction> transactionReader(
            EntityManagerFactory entityManagerFactory) {

        JpaPagingItemReader<BankTransaction> reader =
        		new JpaPagingItemReader<>(entityManagerFactory);

        reader.setName("transactionReader");
        reader.setEntityManagerFactory(entityManagerFactory);

        reader.setQueryString(
                "SELECT t FROM BankTransaction t WHERE t.status = 'SUCCESS' ORDER BY t.id"
        );

        reader.setPageSize(10);

        return reader;
    }

}
