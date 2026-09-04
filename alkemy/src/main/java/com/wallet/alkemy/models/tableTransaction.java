package com.wallet.alkemy.models;

import java.math.BigInteger;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@Entity (name = "Transaction")
@Table (name = "Transaction", indexes = {
    @Index(name = "idx_cuenta_fecha", columnList = "account_number, date_transaction")
})
@NoArgsConstructor
@AllArgsConstructor


public class tableTransaction {

    @Id
    @Column (name = "Id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column (name = "Date_Transaction")
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime dateTransaction;

    @Column (name = "Balance") // Positive for deposits; negative for withdrawals or payments.
    private double balance;

    @Column (name = "Amount")
    private double amount;

    @Column (name = "Type") // Transaction type, such as withdrawal, deposit, or payment.
    private String type;

    @Column (name = "Movement_Type")
    private String movementType;

    @Column (name = "account_number", nullable = false)
    private BigInteger accountNumber;

    @PrePersist
    @PreUpdate
    private void validateAccountNumber() {
        if (accountNumber == null) {
            throw new IllegalStateException("A transaction must have an associated account");
        }
    }

}
