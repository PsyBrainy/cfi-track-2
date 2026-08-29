package com.wallet.alkemy.models;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigInteger;
import java.time.LocalDate;



@Data
@Entity (name = "bankAccount")
@Table (name = "bankAccount")


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
