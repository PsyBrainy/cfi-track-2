package com.wallet.alkemy.repository;

import java.math.BigInteger;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.wallet.alkemy.models.tableBankAccount;

public interface AccountRepository extends JpaRepository<tableBankAccount, Long> {

    /** Finds the account associated with a user identifier. */
    Optional<tableBankAccount> findByIdUser(BigInteger idUser);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    /** Updates the persisted balance for an account and returns affected rows. */
    @Query("update bankAccount c set c.balance = :balance where c.id = :accountId")
    int updateBalance(@Param("accountId") Long accountId, @Param("balance") double balance);
}
