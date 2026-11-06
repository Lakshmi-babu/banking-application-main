package com.infy.bankingApplication.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class TransferRequest {
	
	@NotBlank(message = "Source account is required")
	private String fromAccount;
	
	@NotBlank(message = "Destination account is required")
    private String toAccount;
	
	@NotNull(message = "Amount is required") 
	@Positive(message = "Amount must be greater than zero")
    private BigDecimal amount;

    public TransferRequest() {
    }

	public String getFromAccount() {
		return fromAccount;
	}

	public void setFromAccount(String fromAccount) {
		this.fromAccount = fromAccount;
	}

	public String getToAccount() {
		return toAccount;
	}

	public void setToAccount(String toAccount) {
		this.toAccount = toAccount;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
    
    

}
