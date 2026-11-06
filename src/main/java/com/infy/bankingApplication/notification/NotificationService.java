package com.infy.bankingApplication.notification;

import org.springframework.stereotype.Service;

import com.infy.bankingApplication.entity.BankTransaction;

@Service
public class NotificationService {
	
	public void sendTransactionNotification(BankTransaction transaction) {

        System.out.println(
                "NOTIFICATION: Transaction successful"
                + " | Reference: " + transaction.getReferenceNumber()
                + " | Type: " + transaction.getTransactionType()
                + " | Amount: " + transaction.getAmount()
        );
    }

}
