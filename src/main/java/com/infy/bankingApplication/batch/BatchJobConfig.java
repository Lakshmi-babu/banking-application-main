package com.infy.bankingApplication.batch;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.database.JpaPagingItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.infy.bankingApplication.entity.BankTransaction;

@Configuration
public class BatchJobConfig {
	
	@Bean
    public Step transactionStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            JpaPagingItemReader<BankTransaction> transactionReader,
            ItemProcessor<BankTransaction, BankTransaction> transactionProcessor,
            ItemWriter<BankTransaction> transactionWriter) {

        return new StepBuilder("transactionStep", jobRepository)
                .<BankTransaction, BankTransaction>chunk(10)
                .transactionManager(transactionManager)
                .reader(transactionReader)
                .processor(transactionProcessor)
                .writer(transactionWriter)
                .build();
    }

    @Bean
    public Job transactionJob(
            JobRepository jobRepository,
            Step transactionStep) {

        return new JobBuilder("transactionJob", jobRepository)
                .start(transactionStep)
                .build();
    }

}
