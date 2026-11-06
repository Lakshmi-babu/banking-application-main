package com.infy.bankingApplication.batch;

import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.infy.bankingApplication.entity.BankTransaction;

@Component
public class BankTransactionProcessor implements ItemProcessor<BankTransaction, BankTransaction> {

    @Override
    public BankTransaction process(BankTransaction transaction) {

        System.out.println(
                "Processing transaction: "
                + transaction.getReferenceNumber()
                + " | Amount: "
                + transaction.getAmount()
        );

        return transaction;
    }

}
