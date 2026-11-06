package com.infy.bankingApplication.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "bank_transactions")
public class BankTransaction {
	
	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    private String transactionType;

	    private String sourceAccount;

	    private String destinationAccount;

	    private BigDecimal amount;

	    private LocalDateTime transactionDate;

	    private String status;
	    
	    @Column(unique = true, nullable = false)
	    private String referenceNumber;

	    public BankTransaction() {
	    }

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getTransactionType() {
			return transactionType;
		}

		public void setTransactionType(String transactionType) {
			this.transactionType = transactionType;
		}

		public String getSourceAccount() {
			return sourceAccount;
		}

		public void setSourceAccount(String sourceAccount) {
			this.sourceAccount = sourceAccount;
		}

		public String getDestinationAccount() {
			return destinationAccount;
		}

		public void setDestinationAccount(String destinationAccount) {
			this.destinationAccount = destinationAccount;
		}

		public BigDecimal getAmount() {
			return amount;
		}

		public void setAmount(BigDecimal amount) {
			this.amount = amount;
		}

		public LocalDateTime getTransactionDate() {
			return transactionDate;
		}

		public void setTransactionDate(LocalDateTime transactionDate) {
			this.transactionDate = transactionDate;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}
		
		public String getReferenceNumber() {
		    return referenceNumber;
		}
		
		public void setReferenceNumber(String referenceNumber) {
		    this.referenceNumber = referenceNumber;
		}
	    
	    

}
