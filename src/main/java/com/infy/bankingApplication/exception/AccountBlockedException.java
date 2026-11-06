package com.infy.bankingApplication.exception;

public class AccountBlockedException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

    public AccountBlockedException(String message) {
        super(message);
    }
	

}
