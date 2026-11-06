package com.infy.bankingApplication.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AccountResponse {
	
	private Long id;
    private String accountNumber;
    private BigDecimal balance;
    private String accountType;
    private String status;
    private Long customerId;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    public AccountResponse() {
    }

	public AccountResponse(Long id, String accountNumber, BigDecimal balance, String accountType, String status,
			Long customerId, LocalDateTime createdAt, LocalDateTime updatedAt) {
		super();
		this.id = id;
		this.accountNumber = accountNumber;
		this.balance = balance;
		this.accountType = accountType;
		this.status = status;
		this.customerId = customerId;
		this.createdAt = createdAt;
        this.updatedAt = updatedAt;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

	public String getAccountType() {
		return accountType;
	}

	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Long getCustomerId() {
		return customerId;
	}

	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}
	public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    

}
