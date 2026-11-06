package com.infy.bankingApplication.dto;

import java.math.BigDecimal;

public class WithdrawRequest {
	
	private String accountNumber;
    private BigDecimal amount;

    public WithdrawRequest() {
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
