package com.infy.bankingApplication.batch;

import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.stereotype.Component;

import com.infy.bankingApplication.entity.BankTransaction;

@Component
public class BankTransactionWriter implements ItemWriter<BankTransaction> {

    @Override
    public void write(Chunk<? extends BankTransaction> transactions) {

        for (BankTransaction transaction : transactions) {

            System.out.println(
                    "Writing transaction: "
                    + transaction.getReferenceNumber()
                    + " | Status: "
                    + transaction.getStatus()
            );
        }
    }

}
