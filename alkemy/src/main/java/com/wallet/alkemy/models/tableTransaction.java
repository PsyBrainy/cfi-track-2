package com.wallet.alkemy.models;

import java.math.BigInteger;
import java.time.LocalDate;

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
@Entity (name = "Transaction")
@Table (name = "Transaction")
@NoArgsConstructor
@AllArgsConstructor


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

    @Column(name = "Movement_Type")
    private String movementType; // INGRESO o EGRESO

    @Column (name = "Account_Number")
    private BigInteger AccountNumber;

}
