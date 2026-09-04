package com.wallet.alkemy.dto;

import java.math.BigInteger;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserDTO {

    @NotBlank(message = "Email is required.")
    @Email(message = "The email format is invalid.")
    @Size(max = 100, message = "Email cannot exceed 100 characters.")
    private String email;

    @NotBlank(message = "First name is required.")
    @Size(max = 20, message = "First name cannot exceed 20 characters.")
    private String name;

    @NotBlank(message = "Last name is required.")
    @Size(max = 30, message = "Last name cannot exceed 30 characters.")
    private String lastName;

    @NotNull(message = "Phone number is required.")
    private BigInteger phoneNumber;

    @NotBlank(message = "Birth date is required.")
    @Size(max = 255, message ="Birth date cannot exceed 255 characters.")
    private String birthDate;

    @NotBlank(message = "Password is required.")
    @Size(max = 255, message ="Password cannot exceed 255 characters.")
    private String password;

    @NotBlank(message = "Address is required.")
    @Size(max = 50, message ="Address cannot exceed 50 characters.")
    private String address;

    @NotNull(message = "National ID is required.")
    private BigInteger dni;

    @NotBlank(message = "City is required.")
    @Size(max = 50, message ="City cannot exceed 50 characters.")
    private String city;

    @NotBlank(message = "Province is required.")
    @Size(max = 50, message ="Province cannot exceed 50 characters.")
    private String province;

    @NotBlank(message = "Postal code is required.")
    @Size(max = 15, message ="Postal code cannot exceed 15 characters.")
    private String postalCode;

    @NotBlank(message = "Gender is required.")
    @Size(max = 30, message ="Gender cannot exceed 30 characters.")
    private String gender;

    @NotBlank(message = "Employment status is required.")
    @Size(max = 30, message ="Employment cannot exceed 30 characters.")
    private String employment;

}
