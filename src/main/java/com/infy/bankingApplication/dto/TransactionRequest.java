package com.infy.bankingApplication.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class TransactionRequest {
	
	@NotBlank(message = "Account number is required")
	private String accountNumber; 
	
	@NotNull(message = "Amount is required") 
	@Positive(message = "Amount must be greater than zero")
	private BigDecimal amount;
	
	public TransactionRequest() {
		
	}
	
	

	public TransactionRequest(String accountNumber, BigDecimal amount) {
		super();
		this.accountNumber = accountNumber;
		this.amount = amount;
	}



	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	
	

}
