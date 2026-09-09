package com.wallet.alkemy.repository;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wallet.alkemy.models.tableTransaction;

@Repository
public interface TransactionRepository extends JpaRepository<tableTransaction, Long> {

    /** Busca la última transacción de una cuenta */
    Optional<tableTransaction> findFirstByAccountNumberOrderByIdDesc(BigInteger accountNumber);

    /** 
     * Historial multiusuario dinámico:
     * Cruza la tabla transaction contra bank_account por el ID relacional de la cuenta.
     */
    @Query(value = """
        SELECT t.account_number, t.amount, t.balance, t.date_transaction, t.type, t.movement_type 
        FROM transaction t
        JOIN bank_account b ON CAST(t.account_number AS bigint) = b.account_id
        WHERE b.account_id = :accountId 
        ORDER BY t.date_transaction DESC
        """, nativeQuery = true)
    List<Map<String, Object>> getHistoryByAccount(@Param("accountId") BigInteger accountId);

    /** Agrupador de gastos mensuales dinámico por ID de cuenta */
    @Query(value = """
        SELECT t.movement_type, SUM(t.amount) AS total 
        FROM transaction t
        JOIN bank_account b ON CAST(t.account_number AS bigint) = b.account_id
        WHERE b.account_id = :accountId 
          AND t.type = 'EGRESO' 
        GROUP BY t.movement_type 
        ORDER BY total DESC
        """, nativeQuery = true)
    List<Map<String, Object>> getGroupedExpensesByAccount(@Param("accountId") BigInteger accountId);

    /** Calcula los ingresos mensuales dinámicos del usuario logueado usando su account_id */
    @Query(value = """
        SELECT COALESCE(SUM(t.amount), 0.0) 
        FROM transaction t 
        JOIN bank_account b ON CAST(t.account_number AS bigint) = b.account_id
        WHERE b.account_id = :accountId 
          AND t.type = 'INGRESO' 
          AND t.date_transaction BETWEEN :startOfMonth AND :endOfMonth
        """, nativeQuery = true)
    double getMonthlyIncomeByAccount(
        @Param("accountId") BigInteger accountId, 
        @Param("startOfMonth") LocalDateTime startOfMonth,
        @Param("endOfMonth") LocalDateTime endOfMonth
    );

    /** Calcula los egresos mensuales dinámicos del usuario logueado usando su account_id */
    @Query(value = """
        SELECT COALESCE(SUM(t.amount), 0.0) 
        FROM transaction t 
        JOIN bank_account b ON CAST(t.account_number AS bigint) = b.account_id
        WHERE b.account_id = :accountId 
          AND t.type = 'EGRESO' 
          AND t.date_transaction BETWEEN :startOfMonth AND :endOfMonth
        """, nativeQuery = true)
    double getMonthlyExpensesByAccount(
        @Param("accountId") BigInteger accountId,
        @Param("startOfMonth") LocalDateTime startOfMonth,
        @Param("endOfMonth") LocalDateTime endOfMonth
    );
}
