package com.infy.bankingApplication;

import static org.mockito.Mockito.times;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infy.bankingApplication.dto.TransactionRequest;
import com.infy.bankingApplication.dto.TransferRequest;
import com.infy.bankingApplication.entity.Account;
import com.infy.bankingApplication.entity.BankTransaction;
import com.infy.bankingApplication.entity.IdempotencyKey;
import com.infy.bankingApplication.repository.AccountRepository;
import com.infy.bankingApplication.repository.BankTransactionRepository;
import com.infy.bankingApplication.repository.CustomerRepository;
import com.infy.bankingApplication.repository.IdempotencyKeyRepository;
import com.infy.bankingApplication.repository.OutboxEventRepository;
import com.infy.bankingApplication.exception.AccountBlockedException;
import com.infy.bankingApplication.exception.InsufficientBalanceException;
import com.infy.bankingApplication.exception.ResourceNotFoundException;
import com.infy.bankingApplication.service.AuditLogService;
import com.infy.bankingApplication.service.FailedTransactionService;
import com.infy.bankingApplication.service.impl.AccountServiceImpl;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {
	
	@Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private BankTransactionRepository transactionRepository;

    @Mock
    private IdempotencyKeyRepository idempotencyKeyRepository;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private FailedTransactionService failedTransactionService;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AccountServiceImpl accountService;

    private Account account;

    @BeforeEach
    void setUp() {
        account = new Account();
        account.setAccountNumber("4950727548");
        account.setBalance(new BigDecimal("8930.00"));
        account.setStatus("ACTIVE");
    }

    @Test
    void withdraw_WhenInsufficientBalance_ShouldNotDeductMoney() {

        TransactionRequest request = new TransactionRequest();
        request.setAccountNumber("4950727548");
        request.setAmount(new BigDecimal("99999999.00"));

        when(idempotencyKeyRepository.findByIdempotencyKey("TEST-WITHDRAW-001"))
                .thenReturn(Optional.empty());

        when(accountRepository.findByAccountNumberForUpdate("4950727548"))
                .thenReturn(Optional.of(account));

        assertThrows(
                InsufficientBalanceException.class,
                () -> accountService.withdraw(
                        request, "TEST-WITHDRAW-001"));

        // Balance must remain unchanged.
        assertEquals(
                new BigDecimal("8930.00"),
                account.getBalance());

        // Account must not be saved with a changed balance.
        verify(accountRepository, never()).save(any(Account.class));

        // A failed transaction must be recorded.
        verify(failedTransactionService).saveFailedTransaction(
                "WITHDRAW",
                "4950727548",
                null,
                new BigDecimal("99999999.00"),
                "SYSTEM");

        // A failed transaction must be audited.
        verify(auditLogService).logFailedTransaction(
                "WITHDRAW",
                "4950727548",
                null,
                new BigDecimal("99999999.00"),
                "SYSTEM");

        // No successful transaction should be saved.
        verify(transactionRepository, never())
                .save(any(BankTransaction.class));
    }
    //WITHDRAW
    @Test
    void withdraw_WhenSufficientBalance_ShouldDeductMoney() {

        TransactionRequest request = new TransactionRequest();
        request.setAccountNumber("4950727548");
        request.setAmount(new BigDecimal("500.00"));

        when(idempotencyKeyRepository.findByIdempotencyKey("TEST-WITHDRAW-002"))
                .thenReturn(Optional.empty());

        when(accountRepository.findByAccountNumberForUpdate("4950727548"))
        .thenReturn(Optional.of(account));

when(accountRepository.save(any(Account.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

        BankTransaction savedTransaction = new BankTransaction();
        savedTransaction.setTransactionType("WITHDRAW");
        savedTransaction.setAmount(new BigDecimal("500.00"));
        savedTransaction.setStatus("SUCCESS");

        when(transactionRepository.save(any(BankTransaction.class)))
                .thenReturn(savedTransaction);

        // Execute withdrawal
        accountService.withdraw(request, "TEST-WITHDRAW-002");

        // Verify remaining balance
        assertEquals(new BigDecimal("8430.00"), account.getBalance());

        // Verify account and transaction are saved
        verify(accountRepository).save(account);
        verify(transactionRepository).save(any(BankTransaction.class));
    }
    // DEPOSITE
    
    @Test
    void deposit_WhenValidAmount_ShouldAddMoney() {

        TransactionRequest request = new TransactionRequest();
        request.setAccountNumber("4950727548");
        request.setAmount(new BigDecimal("500.00"));

        when(idempotencyKeyRepository.findByIdempotencyKey("TEST-DEPOSIT-001"))
                .thenReturn(Optional.empty());

        when(accountRepository.findByAccountNumberForUpdate("4950727548"))
                .thenReturn(Optional.of(account));

        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BankTransaction savedTransaction = new BankTransaction();
        savedTransaction.setTransactionType("DEPOSIT");
        savedTransaction.setAmount(new BigDecimal("500.00"));
        savedTransaction.setStatus("SUCCESS");

        when(transactionRepository.save(any(BankTransaction.class)))
                .thenReturn(savedTransaction);

        // Execute deposit
        accountService.deposit(request, "TEST-DEPOSIT-001");

        // Verify updated balance
        assertEquals(new BigDecimal("9430.00"), account.getBalance());

        // Verify account and transaction are saved
        verify(accountRepository).save(account);
        verify(transactionRepository).save(any(BankTransaction.class));

        // Verify audit log and outbox event
        verify(auditLogService).log(
                "DEPOSIT",
                "4950727548",
                null,
                new BigDecimal("500.00"),
                "SUCCESS");

        verify(outboxEventRepository).save(any());

        // Verify idempotency key is saved
        verify(idempotencyKeyRepository).save(any());
    }
    // TRANSFER
    
    @Test
    void transfer_WhenSufficientBalance_ShouldUpdateBothAccounts() {

        Account destination = new Account();
        destination.setAccountNumber("8989015667");
        destination.setBalance(new BigDecimal("12291.00"));
        destination.setStatus("ACTIVE");

        TransferRequest request = new TransferRequest();
        request.setFromAccount("4950727548");
        request.setToAccount("8989015667");
        request.setAmount(new BigDecimal("500.00"));

        when(idempotencyKeyRepository.findByIdempotencyKey("TEST-TRANSFER-001"))
                .thenReturn(Optional.empty());

        when(accountRepository.findByAccountNumberForUpdate("4950727548"))
                .thenReturn(Optional.of(account));

        when(accountRepository.findByAccountNumberForUpdate("8989015667"))
                .thenReturn(Optional.of(destination));

        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BankTransaction savedTransaction = new BankTransaction();
        savedTransaction.setTransactionType("TRANSFER");
        savedTransaction.setAmount(new BigDecimal("500.00"));
        savedTransaction.setStatus("SUCCESS");

        when(transactionRepository.save(any(BankTransaction.class)))
                .thenReturn(savedTransaction);

        // Execute transfer
        accountService.transfer(request, "TEST-TRANSFER-001");

        // Verify both account balances
        assertEquals(new BigDecimal("8430.00"), account.getBalance());
        assertEquals(new BigDecimal("12791.00"), destination.getBalance());

        // Verify both accounts and transaction are saved
        verify(accountRepository, times(2)).save(any(Account.class));
        verify(transactionRepository).save(any(BankTransaction.class));

        // Verify audit, outbox and idempotency operations
        verify(auditLogService).log(
                "TRANSFER",
                "4950727548",
                "8989015667",
                new BigDecimal("500.00"),
                "SUCCESS");

        verify(outboxEventRepository).save(any());
        verify(idempotencyKeyRepository).save(any());
    }
    
    //insufficient-balance transfer test
    @Test
    void transfer_WhenInsufficientBalance_ShouldNotChangeEitherAccount() {

        Account destination = new Account();
        destination.setAccountNumber("8989015667");
        destination.setBalance(new BigDecimal("12291.00"));
        destination.setStatus("ACTIVE");

        TransferRequest request = new TransferRequest();
        request.setFromAccount("4950727548");
        request.setToAccount("8989015667");
        request.setAmount(new BigDecimal("99999999.00"));

        when(idempotencyKeyRepository.findByIdempotencyKey("TEST-TRANSFER-002"))
                .thenReturn(Optional.empty());

        when(accountRepository.findByAccountNumberForUpdate("4950727548"))
                .thenReturn(Optional.of(account));

        when(accountRepository.findByAccountNumberForUpdate("8989015667"))
                .thenReturn(Optional.of(destination));

        // Execute transfer and expect insufficient-balance exception
        assertThrows(
                InsufficientBalanceException.class,
                () -> accountService.transfer(request, "TEST-TRANSFER-002"));

        // Verify neither account balance changes
        assertEquals(new BigDecimal("8930.00"), account.getBalance());
        assertEquals(new BigDecimal("12291.00"), destination.getBalance());

        // Neither account should be saved
        verify(accountRepository, never()).save(any(Account.class));

        // No successful transaction or outbox event should be saved
        verify(transactionRepository, never()).save(any(BankTransaction.class));
        verify(outboxEventRepository, never()).save(any());

        // Failed transaction and audit log should be recorded
        verify(failedTransactionService).saveFailedTransaction(
                "TRANSFER",
                "4950727548",
                "8989015667",
                new BigDecimal("99999999.00"),
                "SYSTEM");

        verify(auditLogService).logFailedTransaction(
                "TRANSFER",
                "4950727548",
                "8989015667",
                new BigDecimal("99999999.00"),
                "SYSTEM");
    }
    
    //invalid transaction amounts
    
    @Test
    void deposit_WhenAmountIsZero_ShouldThrowException() {

        TransactionRequest request = new TransactionRequest();
        request.setAccountNumber("4950727548");
        request.setAmount(BigDecimal.ZERO);

        when(idempotencyKeyRepository.findByIdempotencyKey("TEST-DEPOSIT-INVALID-001"))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> accountService.deposit(
                        request, "TEST-DEPOSIT-INVALID-001"));

        // Balance must remain unchanged
        assertEquals(new BigDecimal("8930.00"), account.getBalance());

        // No successful transaction should be saved
        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any(BankTransaction.class));
        verify(outboxEventRepository, never()).save(any());
    }
    
    //negative deposit amount
    @Test
    void deposit_WhenAmountIsNegative_ShouldThrowException() {

        TransactionRequest request = new TransactionRequest();
        request.setAccountNumber("4950727548");
        request.setAmount(new BigDecimal("-500"));

        when(idempotencyKeyRepository.findByIdempotencyKey("TEST-DEPOSIT-INVALID-002"))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> accountService.deposit(
                        request, "TEST-DEPOSIT-INVALID-002"));

        // Account balance must remain unchanged
        assertEquals(new BigDecimal("8930.00"), account.getBalance());

        // No successful transaction or event should be saved
        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any(BankTransaction.class));
        verify(outboxEventRepository, never()).save(any());
    }
    
    //Test zero-amount withdrawal
    @Test
    void withdraw_WhenAmountIsZero_ShouldThrowException() {

        TransactionRequest request = new TransactionRequest();
        request.setAccountNumber("4950727548");
        request.setAmount(BigDecimal.ZERO);

        when(idempotencyKeyRepository.findByIdempotencyKey("TEST-WITHDRAW-INVALID-001"))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> accountService.withdraw(
                        request, "TEST-WITHDRAW-INVALID-001"));

        // Account balance must remain unchanged
        assertEquals(new BigDecimal("8930.00"), account.getBalance());

        // No account update, transaction, or outbox event
        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any(BankTransaction.class));
        verify(outboxEventRepository, never()).save(any());
    }
    //negative withdrawal amount
    
    @Test
    void withdraw_WhenAmountIsNegative_ShouldThrowException() {

        TransactionRequest request = new TransactionRequest();
        request.setAccountNumber("4950727548");
        request.setAmount(new BigDecimal("-500"));

        when(idempotencyKeyRepository.findByIdempotencyKey("TEST-WITHDRAW-INVALID-002"))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> accountService.withdraw(
                        request, "TEST-WITHDRAW-INVALID-002"));

        // Account balance must remain unchanged
        assertEquals(new BigDecimal("8930.00"), account.getBalance());

        // No account update, transaction, or outbox event
        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any(BankTransaction.class));
        verify(outboxEventRepository, never()).save(any());
    }
    
    //transfer with zero amount
    @Test
    void transfer_WhenAmountIsZero_ShouldThrowException() {

        Account destinationAccount = new Account();
        destinationAccount.setAccountNumber("8989015667");
        destinationAccount.setBalance(new BigDecimal("12291.00"));
        destinationAccount.setStatus("ACTIVE");

        TransferRequest request = new TransferRequest();
        request.setFromAccount("4950727548");
        request.setToAccount("8989015667");
        request.setAmount(BigDecimal.ZERO);

        when(idempotencyKeyRepository.findByIdempotencyKey("TEST-TRANSFER-INVALID-001"))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> accountService.transfer(
                        request, "TEST-TRANSFER-INVALID-001"));

        // Both account balances must remain unchanged
        assertEquals(new BigDecimal("8930.00"), account.getBalance());
        assertEquals(new BigDecimal("12291.00"), destinationAccount.getBalance());

        // No successful transfer should be recorded
        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any(BankTransaction.class));
        verify(outboxEventRepository, never()).save(any());
    }
    
    //transfer with a negative amount
    @Test
    void transfer_WhenAmountIsNegative_ShouldThrowException() {

        Account destinationAccount = new Account();
        destinationAccount.setAccountNumber("8989015667");
        destinationAccount.setBalance(new BigDecimal("12291.00"));
        destinationAccount.setStatus("ACTIVE");

        TransferRequest request = new TransferRequest();
        request.setFromAccount("4950727548");
        request.setToAccount("8989015667");
        request.setAmount(new BigDecimal("-500"));

        when(idempotencyKeyRepository.findByIdempotencyKey("TEST-TRANSFER-INVALID-002"))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> accountService.transfer(
                        request, "TEST-TRANSFER-INVALID-002"));

        // Neither account balance should change
        assertEquals(new BigDecimal("8930.00"), account.getBalance());
        assertEquals(new BigDecimal("12291.00"), destinationAccount.getBalance());

        // No account updates, transaction, or outbox event
        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any(BankTransaction.class));
        verify(outboxEventRepository, never()).save(any());
    }
    //transfer to the same account
    @Test
    void transfer_WhenSourceAndDestinationAreSame_ShouldThrowException() {

        TransferRequest request = new TransferRequest();
        request.setFromAccount("4950727548");
        request.setToAccount("4950727548");
        request.setAmount(new BigDecimal("500"));

        when(idempotencyKeyRepository.findByIdempotencyKey("TEST-TRANSFER-SAME-ACCOUNT"))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> accountService.transfer(
                        request, "TEST-TRANSFER-SAME-ACCOUNT"));

        // Balance must remain unchanged
        assertEquals(new BigDecimal("8930.00"), account.getBalance());

        // No account update, transaction, or outbox event
        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any(BankTransaction.class));
        verify(outboxEventRepository, never()).save(any());
    }
    
    //transfer to a nonexistent destination account
    @Test
    void transfer_WhenDestinationAccountDoesNotExist_ShouldThrowException() {

        TransferRequest request = new TransferRequest();
        request.setFromAccount("4950727548");
        request.setToAccount("9999999999");
        request.setAmount(new BigDecimal("500"));

        when(idempotencyKeyRepository.findByIdempotencyKey("TEST-TRANSFER-NO-DEST"))
                .thenReturn(Optional.empty());

        when(accountRepository.findByAccountNumberForUpdate("4950727548"))
                .thenReturn(Optional.of(account));

        when(accountRepository.findByAccountNumberForUpdate("9999999999"))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> accountService.transfer(
                        request, "TEST-TRANSFER-NO-DEST"));

        // Source account balance must remain unchanged
        assertEquals(new BigDecimal("8930.00"), account.getBalance());

        // No successful transaction or outbox event
        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any(BankTransaction.class));
        verify(outboxEventRepository, never()).save(any());
    }
    
    //duplicate deposit test
    @Test
    void deposit_WhenSameIdempotencyKeyIsReused_ShouldNotDepositTwice() {

        String idempotencyKey = "TEST-DEPOSIT-DUPLICATE-001";

        TransactionRequest request = new TransactionRequest();
        request.setAccountNumber("4950727548");
        request.setAmount(new BigDecimal("500"));

        // Simulate a previously processed deposit request
        IdempotencyKey existingKey = new IdempotencyKey();
        existingKey.setIdempotencyKey(idempotencyKey);
        existingKey.setTransactionId(45L);

        when(idempotencyKeyRepository.findByIdempotencyKey(idempotencyKey))
                .thenReturn(Optional.of(existingKey));

        // Return the original transaction associated with this key
        BankTransaction existingTransaction = new BankTransaction();
        existingTransaction.setId(45L);
        existingTransaction.setTransactionType("DEPOSIT");
        existingTransaction.setSourceAccount(null);
        existingTransaction.setDestinationAccount("4950727548");
        existingTransaction.setAmount(new BigDecimal("500"));
        existingTransaction.setStatus("SUCCESS");

        when(transactionRepository.findById(45L))
                .thenReturn(Optional.of(existingTransaction));

        // Execute the duplicate request
        accountService.deposit(request, idempotencyKey);

        // Balance must not change for the duplicate request
        assertEquals(new BigDecimal("8930.00"), account.getBalance());

        // No new transaction or outbox event should be created
        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any(BankTransaction.class));
        verify(outboxEventRepository, never()).save(any());
    }
    
    @Test
    void withdraw_WhenSameIdempotencyKeyIsReused_ShouldNotWithdrawTwice() {

        String key = "TEST-WITHDRAW-DUPLICATE-001";

        TransactionRequest request = new TransactionRequest();
        request.setAccountNumber("4950727548");
        request.setAmount(new BigDecimal("500"));

        IdempotencyKey existingKey = new IdempotencyKey();
        existingKey.setIdempotencyKey(key);
        existingKey.setTransactionId(46L);

        BankTransaction existingTransaction = new BankTransaction();
        existingTransaction.setId(46L);
        existingTransaction.setTransactionType("WITHDRAW");
        existingTransaction.setSourceAccount("4950727548");
        existingTransaction.setDestinationAccount(null);
        existingTransaction.setAmount(new BigDecimal("500"));
        existingTransaction.setStatus("SUCCESS");

        when(idempotencyKeyRepository.findByIdempotencyKey(key))
                .thenReturn(Optional.of(existingKey));
        when(transactionRepository.findById(46L))
                .thenReturn(Optional.of(existingTransaction));

        accountService.withdraw(request, key);

        assertEquals(new BigDecimal("8930.00"), account.getBalance());

        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any(BankTransaction.class));
        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    void transfer_WhenSameIdempotencyKeyIsReused_ShouldNotTransferTwice() {

        String key = "TEST-TRANSFER-DUPLICATE-001";

        TransferRequest request = new TransferRequest();
        request.setFromAccount("4950727548");
        request.setToAccount("8989015667");
        request.setAmount(new BigDecimal("500"));

        IdempotencyKey existingKey = new IdempotencyKey();
        existingKey.setIdempotencyKey(key);
        existingKey.setTransactionId(47L);

        BankTransaction existingTransaction = new BankTransaction();
        existingTransaction.setId(47L);
        existingTransaction.setTransactionType("TRANSFER");
        existingTransaction.setSourceAccount("4950727548");
        existingTransaction.setDestinationAccount("8989015667");
        existingTransaction.setAmount(new BigDecimal("500"));
        existingTransaction.setStatus("SUCCESS");

        when(idempotencyKeyRepository.findByIdempotencyKey(key))
                .thenReturn(Optional.of(existingKey));
        when(transactionRepository.findById(47L))
                .thenReturn(Optional.of(existingTransaction));

        accountService.transfer(request, key);

        assertEquals(new BigDecimal("8930.00"), account.getBalance());

        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any(BankTransaction.class));
        verify(outboxEventRepository, never()).save(any());
    }
    
    @Test
    void deposit_WhenIdempotencyKeyIsMissing_ShouldThrowException() {
        TransactionRequest request = new TransactionRequest();
        request.setAccountNumber("4950727548");
        request.setAmount(new BigDecimal("500"));

        assertThrows(IllegalArgumentException.class,
                () -> accountService.deposit(request, null));

        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void withdraw_WhenIdempotencyKeyIsMissing_ShouldThrowException() {
        TransactionRequest request = new TransactionRequest();
        request.setAccountNumber("4950727548");
        request.setAmount(new BigDecimal("500"));

        assertThrows(IllegalArgumentException.class,
                () -> accountService.withdraw(request, null));

        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void transfer_WhenIdempotencyKeyIsMissing_ShouldThrowException() {
        TransferRequest request = new TransferRequest();
        request.setFromAccount("4950727548");
        request.setToAccount("8989015667");
        request.setAmount(new BigDecimal("500"));

        assertThrows(IllegalArgumentException.class,
                () -> accountService.transfer(request, null));

        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void deposit_WhenAccountDoesNotExist_ShouldThrowException() {
        TransactionRequest request = new TransactionRequest();
        request.setAccountNumber("9999999999");
        request.setAmount(new BigDecimal("500"));

        when(idempotencyKeyRepository.findByIdempotencyKey("TEST-NO-ACCOUNT-DEP"))
                .thenReturn(Optional.empty());
        when(accountRepository.findByAccountNumberForUpdate("9999999999"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> accountService.deposit(request, "TEST-NO-ACCOUNT-DEP"));

        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any(BankTransaction.class));
    }

    @Test
    void withdraw_WhenAccountDoesNotExist_ShouldThrowException() {
        TransactionRequest request = new TransactionRequest();
        request.setAccountNumber("9999999999");
        request.setAmount(new BigDecimal("500"));

        when(idempotencyKeyRepository.findByIdempotencyKey("TEST-NO-ACCOUNT-WD"))
                .thenReturn(Optional.empty());
        when(accountRepository.findByAccountNumberForUpdate("9999999999"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> accountService.withdraw(request, "TEST-NO-ACCOUNT-WD"));

        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any(BankTransaction.class));
    }

    @Test
    void transfer_WhenSourceAccountDoesNotExist_ShouldThrowException() {
        TransferRequest request = new TransferRequest();
        request.setFromAccount("9999999999");
        request.setToAccount("8989015667");
        request.setAmount(new BigDecimal("500"));

        when(idempotencyKeyRepository.findByIdempotencyKey("TEST-NO-SOURCE"))
                .thenReturn(Optional.empty());
        when(accountRepository.findByAccountNumberForUpdate("9999999999"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> accountService.transfer(request, "TEST-NO-SOURCE"));

        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any(BankTransaction.class));
    }

    @Test
    void transfer_WhenSourceAccountIsBlocked_ShouldThrowException() {

        account.setStatus("BLOCKED");

        Account destination = new Account();
        destination.setAccountNumber("8989015667");
        destination.setBalance(new BigDecimal("12291.00"));
        destination.setStatus("ACTIVE");

        TransferRequest request = new TransferRequest();
        request.setFromAccount("4950727548");
        request.setToAccount("8989015667");
        request.setAmount(new BigDecimal("500"));

        when(idempotencyKeyRepository.findByIdempotencyKey("TEST-BLOCKED-SOURCE"))
                .thenReturn(Optional.empty());

        when(accountRepository.findByAccountNumberForUpdate("4950727548"))
                .thenReturn(Optional.of(account));

        when(accountRepository.findByAccountNumberForUpdate("8989015667"))
                .thenReturn(Optional.of(destination));

        assertThrows(
                AccountBlockedException.class,
                () -> accountService.transfer(request, "TEST-BLOCKED-SOURCE"));

        // Neither account should be changed
        assertEquals(new BigDecimal("8930.00"), account.getBalance());
        assertEquals(new BigDecimal("12291.00"), destination.getBalance());

        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any(BankTransaction.class));
        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    void transfer_WhenDestinationAccountIsBlocked_ShouldThrowException() {
        Account destination = new Account();
        destination.setAccountNumber("8989015667");
        destination.setBalance(new BigDecimal("12291.00"));
        destination.setStatus("BLOCKED");

        TransferRequest request = new TransferRequest();
        request.setFromAccount("4950727548");
        request.setToAccount("8989015667");
        request.setAmount(new BigDecimal("500"));

        when(idempotencyKeyRepository.findByIdempotencyKey("TEST-BLOCKED-DEST"))
                .thenReturn(Optional.empty());
        when(accountRepository.findByAccountNumberForUpdate("4950727548"))
                .thenReturn(Optional.of(account));
        when(accountRepository.findByAccountNumberForUpdate("8989015667"))
                .thenReturn(Optional.of(destination));

        assertThrows(AccountBlockedException.class,
                () -> accountService.transfer(request, "TEST-BLOCKED-DEST"));

        assertEquals(new BigDecimal("8930.00"), account.getBalance());
        assertEquals(new BigDecimal("12291.00"), destination.getBalance());

        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any(BankTransaction.class));
    }

    @Test
    void deposit_WhenAccountIsBlocked_ShouldThrowException() {
        account.setStatus("BLOCKED");

        TransactionRequest request = new TransactionRequest();
        request.setAccountNumber("4950727548");
        request.setAmount(new BigDecimal("500"));

        when(idempotencyKeyRepository.findByIdempotencyKey("TEST-BLOCKED-DEP"))
                .thenReturn(Optional.empty());
        when(accountRepository.findByAccountNumberForUpdate("4950727548"))
                .thenReturn(Optional.of(account));

        assertThrows(AccountBlockedException.class,
                () -> accountService.deposit(request, "TEST-BLOCKED-DEP"));

        assertEquals(new BigDecimal("8930.00"), account.getBalance());
        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any(BankTransaction.class));
    }

    @Test
    void withdraw_WhenAccountIsBlocked_ShouldThrowException() {
        account.setStatus("BLOCKED");

        TransactionRequest request = new TransactionRequest();
        request.setAccountNumber("4950727548");
        request.setAmount(new BigDecimal("500"));

        when(idempotencyKeyRepository.findByIdempotencyKey("TEST-BLOCKED-WD"))
                .thenReturn(Optional.empty());
        when(accountRepository.findByAccountNumberForUpdate("4950727548"))
                .thenReturn(Optional.of(account));

        assertThrows(AccountBlockedException.class,
                () -> accountService.withdraw(request, "TEST-BLOCKED-WD"));

        assertEquals(new BigDecimal("8930.00"), account.getBalance());
        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any(BankTransaction.class));
    }

    @Test
    void deposit_WhenIdempotencyKeyIsReusedWithDifferentAmount_ShouldThrowException() {
        String key = "TEST-DEP-DIFFERENT-AMOUNT";

        TransactionRequest request = new TransactionRequest();
        request.setAccountNumber("4950727548");
        request.setAmount(new BigDecimal("700"));

        IdempotencyKey existingKey = new IdempotencyKey();
        existingKey.setIdempotencyKey(key);
        existingKey.setTransactionId(51L);

        BankTransaction original = new BankTransaction();
        original.setId(51L);
        original.setTransactionType("DEPOSIT");
        original.setSourceAccount(null);
        original.setDestinationAccount("4950727548");
        original.setAmount(new BigDecimal("500"));
        original.setStatus("SUCCESS");

        when(idempotencyKeyRepository.findByIdempotencyKey(key))
                .thenReturn(Optional.of(existingKey));
        when(transactionRepository.findById(51L))
                .thenReturn(Optional.of(original));

        assertThrows(IllegalArgumentException.class,
                () -> accountService.deposit(request, key));

        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any(BankTransaction.class));
    }

    @Test
    void withdraw_WhenKeyBelongsToDifferentTransactionType_ShouldThrowException() {
        String key = "TEST-WD-WRONG-TYPE";

        TransactionRequest request = new TransactionRequest();
        request.setAccountNumber("4950727548");
        request.setAmount(new BigDecimal("500"));

        IdempotencyKey existingKey = new IdempotencyKey();
        existingKey.setIdempotencyKey(key);
        existingKey.setTransactionId(52L);

        BankTransaction original = new BankTransaction();
        original.setId(52L);
        original.setTransactionType("DEPOSIT");
        original.setSourceAccount(null);
        original.setDestinationAccount("4950727548");
        original.setAmount(new BigDecimal("500"));
        original.setStatus("SUCCESS");

        when(idempotencyKeyRepository.findByIdempotencyKey(key))
                .thenReturn(Optional.of(existingKey));
        when(transactionRepository.findById(52L))
                .thenReturn(Optional.of(original));

        assertThrows(IllegalArgumentException.class,
                () -> accountService.withdraw(request, key));

        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any(BankTransaction.class));
    }
    
    @Test
    void transfer_WhenSourceAccountIsBlocked_ShouldNotChangeBalances() {
        account.setStatus("BLOCKED");

        Account destination = new Account();
        destination.setAccountNumber("8989015667");
        destination.setBalance(new BigDecimal("12291.00"));
        destination.setStatus("ACTIVE");

        TransferRequest request = new TransferRequest();
        request.setFromAccount("4950727548");
        request.setToAccount("8989015667");
        request.setAmount(new BigDecimal("500"));

        when(idempotencyKeyRepository.findByIdempotencyKey("ROLLBACK-001"))
                .thenReturn(Optional.empty());
        when(accountRepository.findByAccountNumberForUpdate("4950727548"))
                .thenReturn(Optional.of(account));
        when(accountRepository.findByAccountNumberForUpdate("8989015667"))
                .thenReturn(Optional.of(destination));

        assertThrows(AccountBlockedException.class,
                () -> accountService.transfer(request, "ROLLBACK-001"));

        assertEquals(new BigDecimal("8930.00"), account.getBalance());
        assertEquals(new BigDecimal("12291.00"), destination.getBalance());

        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any(BankTransaction.class));
        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    void transfer_WhenDestinationAccountIsBlocked_ShouldNotChangeBalances() {
        Account destination = new Account();
        destination.setAccountNumber("8989015667");
        destination.setBalance(new BigDecimal("12291.00"));
        destination.setStatus("BLOCKED");

        TransferRequest request = new TransferRequest();
        request.setFromAccount("4950727548");
        request.setToAccount("8989015667");
        request.setAmount(new BigDecimal("500"));

        when(idempotencyKeyRepository.findByIdempotencyKey("ROLLBACK-002"))
                .thenReturn(Optional.empty());
        when(accountRepository.findByAccountNumberForUpdate("4950727548"))
                .thenReturn(Optional.of(account));
        when(accountRepository.findByAccountNumberForUpdate("8989015667"))
                .thenReturn(Optional.of(destination));

        assertThrows(AccountBlockedException.class,
                () -> accountService.transfer(request, "ROLLBACK-002"));

        assertEquals(new BigDecimal("8930.00"), account.getBalance());
        assertEquals(new BigDecimal("12291.00"), destination.getBalance());

        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any(BankTransaction.class));
        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    void transfer_WhenSourceAccountDoesNotExist_ShouldNotSaveTransaction() {
        TransferRequest request = new TransferRequest();
        request.setFromAccount("4950727548");
        request.setToAccount("8989015667");
        request.setAmount(new BigDecimal("500"));

        when(idempotencyKeyRepository.findByIdempotencyKey("ROLLBACK-003"))
                .thenReturn(Optional.empty());
        when(accountRepository.findByAccountNumberForUpdate("4950727548"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> accountService.transfer(request, "ROLLBACK-003"));

        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any(BankTransaction.class));
        verify(outboxEventRepository, never()).save(any());
    }
    
    @Test
    void transfer_WhenSavingTransactionFails_ShouldStopFurtherProcessing() {
        Account destination = new Account();
        destination.setAccountNumber("8989015667");
        destination.setBalance(new BigDecimal("12291.00"));
        destination.setStatus("ACTIVE");

        TransferRequest request = new TransferRequest();
        request.setFromAccount("4950727548");
        request.setToAccount("8989015667");
        request.setAmount(new BigDecimal("500"));

        when(idempotencyKeyRepository.findByIdempotencyKey("ROLLBACK-004"))
                .thenReturn(Optional.empty());

        when(accountRepository.findByAccountNumberForUpdate("4950727548"))
                .thenReturn(Optional.of(account));

        when(accountRepository.findByAccountNumberForUpdate("8989015667"))
                .thenReturn(Optional.of(destination));

        when(transactionRepository.save(any(BankTransaction.class)))
                .thenThrow(new RuntimeException("Simulated database failure"));

        assertThrows(RuntimeException.class,
                () -> accountService.transfer(request, "ROLLBACK-004"));

        // Both account save calls happen before transaction saving.
        verify(accountRepository, times(2)).save(any(Account.class));

        // Processing must stop when transaction saving fails.
        verify(outboxEventRepository, never()).save(any());
        verify(idempotencyKeyRepository, never()).save(any());
    }

}
