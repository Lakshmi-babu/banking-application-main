package com.infy.bankingApplication.controller;



import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import com.infy.bankingApplication.dto.AccountRequest;
import com.infy.bankingApplication.dto.AccountResponse;
import com.infy.bankingApplication.dto.AccountStatusRequest;
import com.infy.bankingApplication.dto.TransactionRequest;
import com.infy.bankingApplication.dto.TransactionResponse;
import com.infy.bankingApplication.dto.TransferRequest;

import com.infy.bankingApplication.service.AccountService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
	
	private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
    		@Valid @RequestBody AccountRequest request) {

        return ResponseEntity.ok(
                accountService.createAccount(request));
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponse> getAccount(
            @PathVariable String accountNumber) {

        return ResponseEntity.ok(
                accountService.getAccount(accountNumber));
    }

    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody TransactionRequest request) {

        return ResponseEntity.ok(
                accountService.deposit(request, idempotencyKey));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody TransactionRequest request) {

        return ResponseEntity.ok(
                accountService.withdraw(request, idempotencyKey));
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(
    		@RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody TransferRequest request) {

        return ResponseEntity.ok(
                accountService.transfer(request, idempotencyKey));
    }
    
//    @GetMapping("/transactions/{accountNumber}")
//    public ResponseEntity<List<TransactionResponse>> getTransactionHistory(
//            @PathVariable String accountNumber) {
//
//        return ResponseEntity.ok(
//                accountService.getTransactionHistory(accountNumber));
//    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{accountNumber}/status")
    public ResponseEntity<AccountResponse> updateAccountStatus(
            @PathVariable String accountNumber,
            @Valid @RequestBody AccountStatusRequest request) {

        return ResponseEntity.ok(
                accountService.updateAccountStatus(
                        accountNumber,
                        request.getStatus()));
    }
    



}
