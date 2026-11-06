package com.infy.bankingApplication.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.infy.bankingApplication.entity.BankTransaction;

public interface BankTransactionRepository extends JpaRepository<BankTransaction, Long> {

	// Existing methods
    Page<BankTransaction> findBySourceAccountOrDestinationAccount(
            String sourceAccount,
            String destinationAccount,
            Pageable pageable);

    Page<BankTransaction> findBySourceAccountOrDestinationAccountAndTransactionType(
            String sourceAccount,
            String destinationAccount,
            String transactionType,
            Pageable pageable);

    @Query("""
            SELECT t
            FROM BankTransaction t
            WHERE (t.sourceAccount = :accountNumber
                   OR t.destinationAccount = :accountNumber)
              AND t.transactionType = :transactionType
            """)
    Page<BankTransaction> findByAccountAndType(
            @Param("accountNumber") String accountNumber,
            @Param("transactionType") String transactionType,
            Pageable pageable);

    @Query("""
            SELECT t
            FROM BankTransaction t
            WHERE (t.sourceAccount = :accountNumber
                   OR t.destinationAccount = :accountNumber)
              AND t.transactionDate BETWEEN :fromDate AND :toDate
            """)
    Page<BankTransaction> findByAccountAndDateRange(
            @Param("accountNumber") String accountNumber,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable);

    @Query("""
            SELECT t
            FROM BankTransaction t
            WHERE (t.sourceAccount = :accountNumber
                   OR t.destinationAccount = :accountNumber)
              AND t.transactionType = :transactionType
              AND t.transactionDate BETWEEN :fromDate AND :toDate
            """)
    Page<BankTransaction> findByAccountTypeAndDateRange(
            @Param("accountNumber") String accountNumber,
            @Param("transactionType") String transactionType,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable);


    // NEW — Status filter
    @Query("""
            SELECT t
            FROM BankTransaction t
            WHERE (t.sourceAccount = :accountNumber
                   OR t.destinationAccount = :accountNumber)
              AND t.status = :status
            """)
    Page<BankTransaction> findByAccountAndStatus(
            @Param("accountNumber") String accountNumber,
            @Param("status") String status,
            Pageable pageable);


    // NEW — Type + Status
    @Query("""
            SELECT t
            FROM BankTransaction t
            WHERE (t.sourceAccount = :accountNumber
                   OR t.destinationAccount = :accountNumber)
              AND t.transactionType = :transactionType
              AND t.status = :status
            """)
    Page<BankTransaction> findByAccountTypeAndStatus(
            @Param("accountNumber") String accountNumber,
            @Param("transactionType") String transactionType,
            @Param("status") String status,
            Pageable pageable);


    // NEW — Type + Status + Date
    @Query("""
            SELECT t
            FROM BankTransaction t
            WHERE (t.sourceAccount = :accountNumber
                   OR t.destinationAccount = :accountNumber)
              AND t.transactionType = :transactionType
              AND t.status = :status
              AND t.transactionDate BETWEEN :fromDate AND :toDate
            """)
    Page<BankTransaction> findByAccountTypeStatusAndDateRange(
            @Param("accountNumber") String accountNumber,
            @Param("transactionType") String transactionType,
            @Param("status") String status,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable);
    
    @Query("""
            SELECT t
            FROM BankTransaction t
            WHERE (t.sourceAccount = :accountNumber
                   OR t.destinationAccount = :accountNumber)
              AND t.transactionDate >= :fromDate
            """)
    Page<BankTransaction> findByAccountAndFromDate(
            @Param("accountNumber") String accountNumber,
            @Param("fromDate") LocalDateTime fromDate,
            Pageable pageable);


    @Query("""
            SELECT t
            FROM BankTransaction t
            WHERE (t.sourceAccount = :accountNumber
                   OR t.destinationAccount = :accountNumber)
              AND t.transactionDate <= :toDate
            """)
    Page<BankTransaction> findByAccountAndToDate(
            @Param("accountNumber") String accountNumber,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable);
    
    @Query("""
            SELECT t
            FROM BankTransaction t
            WHERE (t.sourceAccount = :accountNumber
                   OR t.destinationAccount = :accountNumber)
              AND (:type IS NULL OR t.transactionType = :type)
              AND (:status IS NULL OR t.status = :status)
              AND (:fromDate IS NULL OR t.transactionDate >= :fromDate)
              AND (:toDate IS NULL OR t.transactionDate <= :toDate)
            """)
    Page<BankTransaction> findTransactionHistory(
            @Param("accountNumber") String accountNumber,
            @Param("type") String type,
            @Param("status") String status,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable);
    
    Optional<BankTransaction> findByReferenceNumber(String referenceNumber);
    

    long countBySourceAccountOrDestinationAccount(
            String sourceAccount,
            String destinationAccount);

    @Query("""
            SELECT COUNT(t)
            FROM BankTransaction t
            WHERE (t.sourceAccount = :accountNumber
                   OR t.destinationAccount = :accountNumber)
              AND t.status = :status
            """)
    long countByAccountAndStatus(
            @Param("accountNumber") String accountNumber,
            @Param("status") String status);

    @Query("""
            SELECT COUNT(t)
            FROM BankTransaction t
            WHERE (t.sourceAccount = :accountNumber
                   OR t.destinationAccount = :accountNumber)
              AND t.transactionType = :transactionType
            """)
    long countByAccountAndTransactionType(
            @Param("accountNumber") String accountNumber,
            @Param("transactionType") String transactionType);

    @Query("""
            SELECT COALESCE(SUM(t.amount), 0)
            FROM BankTransaction t
            WHERE (t.sourceAccount = :accountNumber
                   OR t.destinationAccount = :accountNumber)
              AND t.transactionType = :transactionType
              AND t.status = 'SUCCESS'
            """)
    BigDecimal sumAmountByAccountAndTransactionType(
            @Param("accountNumber") String accountNumber,
            @Param("transactionType") String transactionType);
    
    List<BankTransaction> findByAmountBetweenOrderByAmountAsc(
            BigDecimal minAmount,
            BigDecimal maxAmount);
    
    List<BankTransaction> findByTransactionTypeOrderByTransactionDateDesc(
            String transactionType);
    
    List<BankTransaction>
    findByTransactionTypeAndSourceAccountOrTransactionTypeAndDestinationAccountOrderByTransactionDateDesc(
            String sourceType,
            String sourceAccount,
            String destinationType,
            String destinationAccount);
    
    @Query("""
    	    SELECT t FROM BankTransaction t
    	    WHERE t.transactionType = :type
    	      AND (t.sourceAccount = :accountNumber
    	           OR t.destinationAccount = :accountNumber)
    	    ORDER BY t.transactionDate DESC
    	    """)
    	List<BankTransaction> findByAccountAndTransactionType(
    	        @Param("accountNumber") String accountNumber,
    	        @Param("type") String type);
    
    @Query("""
    	    SELECT t FROM BankTransaction t
    	    WHERE t.status = :status
    	      AND (t.sourceAccount = :accountNumber
    	           OR t.destinationAccount = :accountNumber)
    	    ORDER BY t.transactionDate DESC
    	    """)
    	List<BankTransaction> findByAccountAndStatus(
    	        @Param("accountNumber") String accountNumber,
    	        @Param("status") String status);
    
    @Query("""
    	    SELECT t FROM BankTransaction t
    	    WHERE (t.sourceAccount = :accountNumber
    	       OR t.destinationAccount = :accountNumber)
    	      AND UPPER(t.transactionType) = UPPER(:transactionType)
    	      AND UPPER(t.status) = UPPER(:status)
    	    ORDER BY t.transactionDate DESC
    	    """)
    	List<BankTransaction> findByAccountAndTypeAndStatus(
    	        @Param("accountNumber") String accountNumber,
    	        @Param("transactionType") String transactionType,
    	        @Param("status") String status
    	);
}