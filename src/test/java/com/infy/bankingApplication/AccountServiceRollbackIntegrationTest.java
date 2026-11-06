package com.infy.bankingApplication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import com.infy.bankingApplication.dto.TransferRequest;
import com.infy.bankingApplication.entity.Account;
import com.infy.bankingApplication.entity.Customer;
import com.infy.bankingApplication.repository.AccountRepository;
import com.infy.bankingApplication.repository.AuditLogRepository;
import com.infy.bankingApplication.repository.BankTransactionRepository;
import com.infy.bankingApplication.repository.CustomerRepository;
import com.infy.bankingApplication.repository.IdempotencyKeyRepository;
import com.infy.bankingApplication.repository.OutboxEventRepository;
import com.infy.bankingApplication.service.AccountService;

@SpringBootTest(properties = {
	    "app.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="
	})
@ActiveProfiles("test")
@Import(KafkaTestConfiguration.class)
public class AccountServiceRollbackIntegrationTest {
	
	@Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private BankTransactionRepository transactionRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private IdempotencyKeyRepository idempotencyKeyRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void transferShouldRollbackDatabaseChangesWhenIdempotencyInsertFails() {

        String suffix = String.format(
                "%08d",
                Math.abs(System.nanoTime() % 100_000_000L));

        String sourceNumber = "71" + suffix;
        String destinationNumber = "72" + suffix;
        String idempotencyKey = "ROLLBACK-" + suffix;

        Customer customer = new Customer();
        customer.setFirstName("Rollback");
        customer.setLastName("Test");
        customer.setEmail("rollback-" + suffix + "@example.com");
        customer.setPhone("9123456789");
        customer = customerRepository.saveAndFlush(customer);

        Account source = new Account();
        source.setAccountNumber(sourceNumber);
        source.setAccountType("SAVINGS");
        source.setBalance(new BigDecimal("1000.00"));
        source.setStatus("ACTIVE");
        source.setCustomer(customer);

        Account destination = new Account();
        destination.setAccountNumber(destinationNumber);
        destination.setAccountType("SAVINGS");
        destination.setBalance(new BigDecimal("500.00"));
        destination.setStatus("ACTIVE");
        destination.setCustomer(customer);

        accountRepository.saveAndFlush(source);
        accountRepository.saveAndFlush(destination);

        long transactionsBefore = transactionRepository.count();
        long outboxBefore = outboxEventRepository.count();
        long idempotencyBefore = idempotencyKeyRepository.count();
        long auditsBefore = auditLogRepository.count();

        String constraintName = "chk_reject_rollback_test_key";
        boolean constraintCreated = false;

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "rollback-test-user",
                        "N/A",
                        List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))));

        try {
            jdbcTemplate.execute(
                    "ALTER TABLE idempotency_keys " +
                    "ADD CONSTRAINT " + constraintName + " " +
                    "CHECK (idempotency_key NOT LIKE 'ROLLBACK-%')");

            constraintCreated = true;

            TransferRequest request = new TransferRequest();
            request.setFromAccount(sourceNumber);
            request.setToAccount(destinationNumber);
            request.setAmount(new BigDecimal("200.00"));

            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> accountService.transfer(request, idempotencyKey));

            // Verify that the exception identifies a database constraint failure.
            assertTrue(
                    hasConstraintViolationCause(exception),
                    "Expected the transfer to fail because of the idempotency-key constraint. " +
                    "Actual exception: " + exception);

        } finally {
            try {
                if (constraintCreated) {
                    jdbcTemplate.execute(
                            "ALTER TABLE idempotency_keys " +
                            "DROP CONSTRAINT " + constraintName);
                }
            } finally {
                SecurityContextHolder.clearContext();
            }
        }

        Account sourceAfter =
                accountRepository.findByAccountNumber(sourceNumber)
                        .orElseThrow();

        Account destinationAfter =
                accountRepository.findByAccountNumber(destinationNumber)
                        .orElseThrow();

        assertEquals(
                0,
                new BigDecimal("1000.00")
                        .compareTo(sourceAfter.getBalance()),
                "Source balance should roll back");

        assertEquals(
                0,
                new BigDecimal("500.00")
                        .compareTo(destinationAfter.getBalance()),
                "Destination balance should roll back");

        assertEquals(
                transactionsBefore,
                transactionRepository.count(),
                "Transaction record should roll back");

        assertEquals(
                outboxBefore,
                outboxEventRepository.count(),
                "Outbox event should roll back");

        assertEquals(
                idempotencyBefore,
                idempotencyKeyRepository.count(),
                "Idempotency record should not remain");

        assertEquals(
                auditsBefore,
                auditLogRepository.count(),
                "Audit record should roll back");
    }

    private boolean hasConstraintViolationCause(Throwable exception) {
        Throwable current = exception;

        while (current != null) {
            if (current instanceof DataIntegrityViolationException) {
                return true;
            }

            String message = current.getMessage();
            if (message != null &&
                    (message.contains("chk_reject_rollback_test_key")
                    || message.contains("Check constraint"))) {
                return true;
            }

            current = current.getCause();
        }

        return false;
    }
}
