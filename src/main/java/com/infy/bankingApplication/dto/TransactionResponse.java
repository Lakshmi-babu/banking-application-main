package com.infy.bankingApplication.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponse {
	
	private Long id;
	private String referenceNumber;
	private String transactionType;
	private String sourceAccount;
	private String destinationAccount; 
	private BigDecimal amount;
	private LocalDateTime transactionDate;
	private String status; 
	
	public TransactionResponse() {
		
	}

	public TransactionResponse(Long id, String referenceNumber, String transactionType, String sourceAccount, String destinationAccount,
			BigDecimal amount, LocalDateTime transactionDate, String status) {
		super();
		this.id = id;
		this.referenceNumber = referenceNumber;
		this.transactionType = transactionType;
		this.sourceAccount = sourceAccount;
		this.destinationAccount = destinationAccount;
		this.amount = amount;
		this.transactionDate = transactionDate;
		this.status = status;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public String getReferenceNumber() {
	    return referenceNumber;
	}

	public void setReferenceNumber(String referenceNumber) {
	    this.referenceNumber = referenceNumber;
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
	
	

}
