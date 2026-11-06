package com.infy.bankingApplication.service;



import com.infy.bankingApplication.dto.AccountRequest;
import com.infy.bankingApplication.dto.AccountResponse;
import com.infy.bankingApplication.dto.TransactionRequest;
import com.infy.bankingApplication.dto.TransactionResponse;
import com.infy.bankingApplication.dto.TransferRequest;


public interface AccountService {
	
	 AccountResponse createAccount(AccountRequest request);

	    AccountResponse getAccount(String accountNumber);
	    
	    AccountResponse updateAccountStatus(
	            String accountNumber,
	            String status);

	    TransactionResponse deposit(TransactionRequest request,String idempotencyKey);
	    
	    TransactionResponse withdraw(TransactionRequest request,String idempotencyKey);
	    
	    TransactionResponse transfer(TransferRequest request,String idempotencyKey);
	    
	    //List<TransactionResponse> getTransactionHistory(String accountNumber);

}
