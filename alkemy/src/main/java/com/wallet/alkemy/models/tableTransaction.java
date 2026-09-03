package com.wallet.alkemy.models;

import com.wallet.alkemy.config.tableTransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigInteger;
import java.time.LocalDate;



@Getter
@Entity (name = "Transactions")
@Table (name = "Transactions")



public class tableTransaction {

    @Id
    @Column (name = "Id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column (name = "Date_Transaction")
    private LocalDate dateTransaction;

    @Column (name = "Balance") // Balance es positivo para depositos, Balance negativo para extracciones o pagos
    private double balance;

    @Column (name = "Amount")
    private double amount;

    @Enumerated(EnumType.STRING)
    @Column (name = "Type") // Type es el tipo de transaccion (Extraccion, deposito, pago, etc)
    private tableTransactionType type;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private tableUser AccountNumber;



}
