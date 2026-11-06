package com.infy.bankingApplication.dto;

import java.math.BigDecimal;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class AccountRequest {
	
	@NotNull(message = "Customer ID is required")
	private Long customerId;

	@NotBlank(message = "Account type is required")
	private String accountType;

	@NotNull(message = "Initial deposit is required")
	@Positive(message = "Initial deposit must be greater than zero")
	private BigDecimal initialDeposit;

    public AccountRequest() {
    	
    }

	public Long getCustomerId() {
		return customerId;
	}

	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}

	public String getAccountType() {
		return accountType;
	}

	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}

	public BigDecimal getInitialDeposit() {
		return initialDeposit;
	}

	public void setInitialDeposit(BigDecimal initialDeposit) {
		this.initialDeposit = initialDeposit;
	}
    
    
	
	

}
