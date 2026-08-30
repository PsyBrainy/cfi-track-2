package com.wallet.alkemy.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDate;



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
