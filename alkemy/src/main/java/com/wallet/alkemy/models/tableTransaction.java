package com.wallet.alkemy.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import java.math.BigInteger;
import java.time.LocalDate;



@Data
@Entity (name = "Transaction")
@Table (name = "Transaction")


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

    @Column (name = "Type") // Type es el tipo de transaccion (Extraccion, deposito, pago, etc)
    private String type;

    @Column (name = "Account_Number")
    private BigInteger AccountNumber;

}
