package com.wallet.alkemy.models;

import java.math.BigInteger;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@Entity (name = "bankAccount")
@Table (name = "bankAccount")
@NoArgsConstructor
@AllArgsConstructor


public class tableBankAccount {

    @Id
    @Column (name = "Id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private tableUser idUser;

    @Column (name = "is_Active")
    private boolean isActive;

    @Column (name = "Credit_Card")
    private boolean creditCard;

    @Column (name = "Debit_Card")
    private boolean debitCard;

    @Column (name = "Currency")
    private String currency;

}
