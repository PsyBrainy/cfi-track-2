package com.wallet.alkemy.models;

import java.math.BigInteger;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

    @Column (name = "Id_User")
    private BigInteger idUser;

    @Column (name = "is_Active")
    private boolean isActive;

    @Column (name = "Credit_Card")
    private boolean creditCard;

    @Column (name = "Debit_Card")
    private boolean debitCard;

    @Column (name = "Currency")
    private String currency;

}
