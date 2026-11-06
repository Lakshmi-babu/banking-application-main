package com.infy.bankingApplication.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

@Entity
public class Account {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

	@Column(nullable = false, unique = true, length = 10)
	private String accountNumber;

	@Column(nullable = false, length = 20)
	private String accountType;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal balance;

	@Column(nullable = false, length = 10)
	private String status;

	@ManyToOne
	@JoinColumn(name = "customer_id", nullable = false)
	private Customer customer;
    
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
    
	@PrePersist
	protected void onCreate() {
	    LocalDateTime now = LocalDateTime.now();
	    createdAt = now;
	    updatedAt = now;

	    System.out.println("Account @PrePersist called: " + now);
	}

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public Account() {
    }

	public Account(Long id, String accountNumber, String accountType, BigDecimal balance, String status) {
		super();
		this.id = id;
		this.accountNumber = accountNumber;
		this.accountType = accountType;
		this.balance = balance;
		this.status = status;
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

	public String getAccountType() {
		return accountType;
	}

	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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
