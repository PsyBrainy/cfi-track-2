package com.wallet.alkemy.models;

import jakarta.persistence.*;
import lombok.Data;
import org.antlr.v4.runtime.misc.NotNull;
import java.math.BigInteger;
import java.time.LocalDate;


@Entity (name = "Users")
@Table (name = "Users")
@Data


public class tableUser {

    @Id
    @Column (name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column (name = "Name")
    private String name;

    @Column (name = "Last Name")
    private String lastName;

    @Column (name = "Email", nullable = false, unique = true)
    private String email;

    @Column (name = "Date_Created")
    private LocalDate dateCreated;

    @Column (name = "Last_Login")
    private LocalDate lastLogin;

    @Column (name = "is_Active")
    private boolean isActive;

    @Column (name = "City")
    private String city;

    @Column (name = "Province")
    private String province;

    @Column (name = "Country")
    private String country;

    @Column (name = "Birthdate")
    private LocalDate birthDate;

    @Column (name = "Address")
    private String address;

    @Column (name = "DNI")
    private BigInteger dni;

    @Column (name = "Postal_Code")
    private String postalCode;

    @Column (name = "Employment")
    private String employment;

    @Column (name = "Gender")
    private String gender;

    @Column (name = "Phone_Number")
    private BigInteger phoneNumber;

    @Column (name = "Password")
    private String password;


}
