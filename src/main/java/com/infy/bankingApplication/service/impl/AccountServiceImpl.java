package com.infy.bankingApplication.service.impl;

import com.infy.bankingApplication.kafka.TransactionEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.cache.annotation.CacheEvict;

import org.springframework.cache.annotation.Caching;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infy.bankingApplication.dto.AccountRequest;
import com.infy.bankingApplication.dto.AccountResponse;
import com.infy.bankingApplication.dto.TransactionRequest;
import com.infy.bankingApplication.dto.TransactionResponse;
import com.infy.bankingApplication.dto.TransferRequest;
import com.infy.bankingApplication.entity.Account;
import com.infy.bankingApplication.entity.BankTransaction;
import com.infy.bankingApplication.entity.Customer;
import com.infy.bankingApplication.entity.IdempotencyKey;
import com.infy.bankingApplication.entity.OutboxEvent;
import com.infy.bankingApplication.exception.AccountBlockedException;
import com.infy.bankingApplication.exception.InsufficientBalanceException;
import com.infy.bankingApplication.exception.ResourceNotFoundException;
import com.infy.bankingApplication.repository.AccountRepository;
import com.infy.bankingApplication.repository.BankTransactionRepository;
import com.infy.bankingApplication.repository.CustomerRepository;
import com.infy.bankingApplication.repository.IdempotencyKeyRepository;
import com.infy.bankingApplication.repository.OutboxEventRepository;
import com.infy.bankingApplication.service.AccountService;
import com.infy.bankingApplication.service.AuditLogService;
import com.infy.bankingApplication.service.FailedTransactionService;

@Service
public class AccountServiceImpl implements AccountService {

	private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final BankTransactionRepository transactionRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final AuditLogService auditLogService;
   // private final TransactionProducer transactionProducer;
    private final FailedTransactionService failedTransactionService;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public AccountServiceImpl(
            AccountRepository accountRepository,
            CustomerRepository customerRepository,
            BankTransactionRepository transactionRepository,
            IdempotencyKeyRepository idempotencyKeyRepository,
            AuditLogService auditLogService,
            FailedTransactionService failedTransactionService,OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper) {

        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
        this.idempotencyKeyRepository = idempotencyKeyRepository;
        this.auditLogService = auditLogService;
       // this.transactionProducer = transactionProducer;
        this.failedTransactionService = failedTransactionService;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    // =========================
    // CREATE ACCOUNT
    // =========================

    @Override
    public AccountResponse createAccount(AccountRequest request) {

        Customer customer = customerRepository
                .findById(request.getCustomerId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Customer not found"));

        Account account = new Account();
        account.setAccountNumber(generateAccountNumber());
        account.setCustomer(customer);
        account.setAccountType(request.getAccountType());
        account.setBalance(request.getInitialDeposit());
        account.setStatus("ACTIVE");
        
        System.out.println("Before save - createdAt: " + account.getCreatedAt());
        

        Account savedAccount = accountRepository.save(account);
        
        System.out.println("After save - createdAt: " + savedAccount.getCreatedAt());

        return toAccountResponse(savedAccount);
    }

    // =========================
    // GET ACCOUNT
    // CACHING DISABLED FOR NOW
    // =========================

    @Override
    public AccountResponse getAccount(String accountNumber) {

        Account account = findAccount(accountNumber);

        return toAccountResponse(account);
    }

    // =========================
    // DEPOSIT
    // =========================

    @Override
    @Transactional
    public TransactionResponse deposit(
            TransactionRequest request,
            String idempotencyKey) {

        validateIdempotencyKey(idempotencyKey);

        Optional<IdempotencyKey> existingKey =
                idempotencyKeyRepository
                        .findByIdempotencyKey(idempotencyKey);

        if (existingKey.isPresent()) {
            return findExistingTransaction(
                    existingKey.get(),
                    "DEPOSIT",
                    null,
                    request.getAccountNumber(),
                    request.getAmount());
        }

        validateAmount(request.getAmount());

        Account account =
                findAccountForUpdate(request.getAccountNumber());

        validateAccountStatus(account);

        account.setBalance(
                account.getBalance().add(request.getAmount()));

        Account savedAccount = accountRepository.save(account);

        BankTransaction transaction = saveTransaction(
                "DEPOSIT",
                null,
                savedAccount.getAccountNumber(),
                request.getAmount(),
                "SUCCESS");

        //transactionProducer.sendTransaction(transaction);
        saveOutboxEvent(transaction);

        auditLogService.log(
                "DEPOSIT",
                request.getAccountNumber(),
                null,
                request.getAmount(),
                "SUCCESS");

        saveIdempotencyKey(idempotencyKey, transaction);

        return convertToTransactionResponse(transaction);
    }

    // =========================
    // WITHDRAW
    // =========================

    @Override
    @Transactional
    public TransactionResponse withdraw(
            TransactionRequest request,
            String idempotencyKey) {

        validateIdempotencyKey(idempotencyKey);

        Optional<IdempotencyKey> existingKey =
                idempotencyKeyRepository
                        .findByIdempotencyKey(idempotencyKey);

        if (existingKey.isPresent()) {
            return findExistingTransaction(
                    existingKey.get(),
                    "WITHDRAW",
                    request.getAccountNumber(),
                    null,
                    request.getAmount());
        }

        validateAmount(request.getAmount());

        Account account =
                findAccountForUpdate(request.getAccountNumber());

        validateAccountStatus(account);

        if (account.getBalance().compareTo(request.getAmount()) < 0) {

            
        	failedTransactionService.saveFailedTransaction(
        	        "WITHDRAW",
        	        account.getAccountNumber(),
        	        null,
        	        request.getAmount(),
        	        getCurrentUsername());

            auditLogService.logFailedTransaction(
                    "WITHDRAW",
                    account.getAccountNumber(),
                    null,
                    request.getAmount(),
                    getCurrentUsername());

           // transactionProducer.sendTransaction(failedTransaction);
            

            throw new InsufficientBalanceException(
                    "Insufficient balance");
        }

        account.setBalance(
                account.getBalance().subtract(request.getAmount()));

        Account savedAccount = accountRepository.save(account);

        BankTransaction transaction = saveTransaction(
                "WITHDRAW",
                savedAccount.getAccountNumber(),
                null,
                request.getAmount(),
                "SUCCESS");

       // transactionProducer.sendTransaction(transaction);
        saveOutboxEvent(transaction);

        auditLogService.log(
                "WITHDRAW",
                request.getAccountNumber(),
                null,
                request.getAmount(),
                "SUCCESS");

        saveIdempotencyKey(idempotencyKey, transaction);

        return convertToTransactionResponse(transaction);
    }

    // =========================
    // TRANSFER
    // =========================

    @Override
    @Transactional
    public TransactionResponse transfer(
            TransferRequest request,
            String idempotencyKey) {

        validateIdempotencyKey(idempotencyKey);

        Optional<IdempotencyKey> existingKey =
                idempotencyKeyRepository
                        .findByIdempotencyKey(idempotencyKey);

        if (existingKey.isPresent()) {
            return findExistingTransaction(
                    existingKey.get(),
                    "TRANSFER",
                    request.getFromAccount(),
                    request.getToAccount(),
                    request.getAmount());
        }

        validateAmount(request.getAmount());

        if (request.getFromAccount() == null
                || request.getToAccount() == null) {
            throw new IllegalArgumentException(
                    "Source and destination accounts are required");
        }

        if (request.getFromAccount()
                .equals(request.getToAccount())) {

            throw new IllegalArgumentException(
                    "Source and destination accounts cannot be same");
        }

        Account source =
                findAccountForUpdate(request.getFromAccount());

        Account destination =
                findAccountForUpdate(request.getToAccount());

        validateAccountStatus(source);
        validateAccountStatus(destination);

        if (source.getBalance().compareTo(request.getAmount()) < 0) {

            
        	failedTransactionService.saveFailedTransaction(
        	        "TRANSFER",
        	        source.getAccountNumber(),
        	        destination.getAccountNumber(),
        	        request.getAmount(),
        	        getCurrentUsername());

            auditLogService.logFailedTransaction(
                    "TRANSFER",
                    source.getAccountNumber(),
                    destination.getAccountNumber(),
                    request.getAmount(),
                    getCurrentUsername());

          //  transactionProducer.sendTransaction(failedTransaction);

            throw new InsufficientBalanceException(
                    "Insufficient balance");
        }

        // TEMPORARY ROLLBACK TEST — KEEP DISABLED
        // throw new RuntimeException("Testing transaction rollback");

        source.setBalance(
                source.getBalance().subtract(request.getAmount()));

        destination.setBalance(
                destination.getBalance().add(request.getAmount()));

        accountRepository.save(source);
        accountRepository.save(destination);

        BankTransaction transaction = saveTransaction(
                "TRANSFER",
                request.getFromAccount(),
                request.getToAccount(),
                request.getAmount(),
                "SUCCESS");

        //transactionProducer.sendTransaction(transaction);
        saveOutboxEvent(transaction);

        auditLogService.log(
                "TRANSFER",
                request.getFromAccount(),
                request.getToAccount(),
                request.getAmount(),
                "SUCCESS");

        saveIdempotencyKey(idempotencyKey, transaction);

        return convertToTransactionResponse(transaction);
    }

    // =========================
    // UPDATE ACCOUNT STATUS
    // =========================

    @Override
    @Caching(evict = {
            @CacheEvict(value = "accounts", key = "#accountNumber")
    })
    public AccountResponse updateAccountStatus(
            String accountNumber,
            String status) {

        Account account = findAccount(accountNumber);

        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException(
                    "Account status is required");
        }

        String newStatus = status.toUpperCase();

        if (!newStatus.equals("ACTIVE")
                && !newStatus.equals("BLOCKED")
                && !newStatus.equals("CLOSED")) {

            throw new IllegalArgumentException(
                    "Invalid account status. Allowed values: ACTIVE, BLOCKED, CLOSED");
        }

        if ("CLOSED".equals(account.getStatus())
                && !"CLOSED".equals(newStatus)) {

            throw new IllegalArgumentException(
                    "Closed account cannot be reactivated or unblocked");
        }

        account.setStatus(newStatus);

        Account savedAccount = accountRepository.save(account);

        auditLogService.log(
                "UPDATE_ACCOUNT_STATUS",
                accountNumber,
                null,
                null,
                newStatus);

        return toAccountResponse(savedAccount);
    }

    // =========================
    // FIND ACCOUNT
    // =========================

    private Account findAccount(String accountNumber) {

        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Account not found"));
    }

    private Account findAccountForUpdate(String accountNumber) {

        return accountRepository.findByAccountNumberForUpdate(accountNumber)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Account not found"));
    }

    // =========================
    // VALIDATION
    // =========================

    private void validateAmount(BigDecimal amount) {

        if (amount == null
                || amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Amount must be greater than zero");
        }
    }

    private void validateIdempotencyKey(String idempotencyKey) {

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException(
                    "Idempotency-Key header is required");
        }
    }

    private void validateAccountStatus(Account account) {

        if (!"ACTIVE".equals(account.getStatus())) {
            throw new AccountBlockedException(
                    "Account is not active. Current status: "
                            + account.getStatus());
        }
    }

    // =========================
    // IDEMPOTENCY
    // =========================

    private TransactionResponse findExistingTransaction(
            IdempotencyKey key,
            String transactionType,
            String sourceAccount,
            String destinationAccount,
            BigDecimal amount) {

        Long transactionId = key.getTransactionId();

        BankTransaction existingTransaction =
                transactionRepository.findById(transactionId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Transaction not found"));

        boolean sameType = transactionType.equals(
                existingTransaction.getTransactionType());

        boolean sameSource = java.util.Objects.equals(
                sourceAccount,
                existingTransaction.getSourceAccount());

        boolean sameDestination = java.util.Objects.equals(
                destinationAccount,
                existingTransaction.getDestinationAccount());

        boolean sameAmount = amount.compareTo(
                existingTransaction.getAmount()) == 0;

        if (!sameType || !sameSource || !sameDestination || !sameAmount) {
            throw new IllegalArgumentException(
                    "Idempotency-Key was already used for a different request");
        }

        return convertToTransactionResponse(existingTransaction);
    }

    private void saveIdempotencyKey(
            String idempotencyKey,
            BankTransaction transaction) {

        IdempotencyKey key = new IdempotencyKey();

        key.setIdempotencyKey(idempotencyKey);
        key.setTransactionId(transaction.getId());
        key.setCreatedAt(LocalDateTime.now());

        idempotencyKeyRepository.save(key);
    }

    // =========================
    // SAVE TRANSACTION
    // =========================

    private BankTransaction saveTransaction(
            String type,
            String source,
            String destination,
            BigDecimal amount,
            String status) {

        BankTransaction transaction = new BankTransaction();

        transaction.setReferenceNumber(generateReferenceNumber());
        transaction.setTransactionType(type);
        transaction.setSourceAccount(source);
        transaction.setDestinationAccount(destination);
        transaction.setAmount(amount);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setStatus(status);

        return transactionRepository.save(transaction);
    }

    // =========================
    // RESPONSE CONVERSION
    // =========================

    private AccountResponse toAccountResponse(Account account) {

        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getBalance(),
                account.getAccountType(),
                account.getStatus(),
                account.getCustomer().getId(),
                account.getCreatedAt(),
                account.getUpdatedAt());
    }

    private TransactionResponse convertToTransactionResponse(
            BankTransaction transaction) {

        return new TransactionResponse(
                transaction.getId(),
                transaction.getReferenceNumber(),
                transaction.getTransactionType(),
                transaction.getSourceAccount(),
                transaction.getDestinationAccount(),
                transaction.getAmount(),
                transaction.getTransactionDate(),
                transaction.getStatus());
    }

    // =========================
    // ACCOUNT NUMBER GENERATION
    // =========================

    private String generateAccountNumber() {

        long number = ThreadLocalRandom.current()
                .nextLong(1_000_000_000L, 10_000_000_000L);

        return String.valueOf(number);
    }

    private String generateReferenceNumber() {

        long number = ThreadLocalRandom.current()
                .nextLong(100_000_000L, 1_000_000_000L);

        return "TXN-" + number;
    }

    // =========================
    // CURRENT USER
    // =========================

    private String getCurrentUsername() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {

            return "SYSTEM";
        }

        return authentication.getName();
    }
    
    private void saveOutboxEvent(
            BankTransaction transaction) {

        TransactionEvent event = new TransactionEvent();

        event.setTransactionReference(
                transaction.getReferenceNumber());

        String accountNumber;

        if ("DEPOSIT".equals(transaction.getTransactionType())) {
            accountNumber = transaction.getDestinationAccount();
        } else {
            accountNumber = transaction.getSourceAccount();
        }

        event.setAccountNumber(accountNumber);
        event.setTransactionType(
                transaction.getTransactionType());

        event.setAmount(
                transaction.getAmount().doubleValue());

        event.setStatus(transaction.getStatus());

        event.setUsername(getCurrentUsername());

        event.setMessage(
                transaction.getTransactionType()
                        + " transaction completed with status "
                        + transaction.getStatus());

        try {
            OutboxEvent outboxEvent = new OutboxEvent();

            outboxEvent.setAggregateType("BankTransaction");
            outboxEvent.setEventType(
                    transaction.getTransactionType());
            outboxEvent.setAggregateId(
                    transaction.getReferenceNumber());

            outboxEvent.setPayload(
                    objectMapper.writeValueAsString(event));

            outboxEvent.setStatus("PENDING");
            outboxEvent.setCreatedAt(LocalDateTime.now());

            outboxEventRepository.save(outboxEvent);

        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Failed to serialize transaction event", e);
        }
    }


}
