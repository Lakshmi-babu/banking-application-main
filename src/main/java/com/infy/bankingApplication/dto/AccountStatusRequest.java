package com.infy.bankingApplication.dto;

import jakarta.validation.constraints.NotBlank;

public class AccountStatusRequest {
	
	@NotBlank(message = "Status is required")
    private String status;

    public AccountStatusRequest() {
    }
    
    

	public AccountStatusRequest(String status) {
		super();
		this.status = status;
	}



	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
    
    

}
