package com.wallet.alkemy.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.wallet.alkemy.dto.GroupedExpenseDTO;
import com.wallet.alkemy.dto.TransactionHistoryDTO;
import com.wallet.alkemy.models.tableTransaction;

public interface TransactionRepository extends JpaRepository<tableTransaction, Long> {

    /** Finds the latest transaction for an account, which provides its current balance. */
    Optional<tableTransaction> findFirstByAccountNumberOrderByIdDesc(java.math.BigInteger accountNumber);

        @Query("""
            SELECT new com.wallet.alkemy.dto.TransactionHistoryDTO(
            t.accountNumber, t.amount, t.balance, t.dateTransaction, t.type, t.movementType)
            FROM Transaction t
            WHERE t.accountNumber = :accountNumber
            ORDER BY t.dateTransaction DESC
            """)
        /** Returns account transactions from newest to oldest. */
        java.util.List<TransactionHistoryDTO> getHistoryByAccount(
            @Param("accountNumber") java.math.BigInteger accountNumber);

        @Query("""
            SELECT new com.wallet.alkemy.dto.GroupedExpenseDTO(t.movementType, SUM(t.amount))
            FROM Transaction t
            WHERE t.accountNumber = :accountNumber
              AND t.type = 'EGRESO'
            GROUP BY t.movementType
            ORDER BY SUM(t.amount) DESC
            """)
        /** Summarizes outgoing transactions by movement type. */
        java.util.List<GroupedExpenseDTO> getGroupedExpensesByAccount(
            @Param("accountNumber") java.math.BigInteger accountNumber);
}