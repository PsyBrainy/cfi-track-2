package com.wallet.alkemy.dto;

import java.math.BigInteger;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserDto {

    @NotBlank(message = "El correo electrónico es obligatorio.")
    @Email(message = "El formato del correo electrónico ingresado no es válido.")
    @Size(max = 100, message = "El correo electrónico no puede superar los 100 caracteres.")
    private String email;

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 20, message = "El nombre no puede superar los 20 caracteres.")
    private String name;

    @NotBlank(message = "El apellido es obligatorio.")
    @Size(max = 30, message = "El apellido no puede superar los 30 caracteres.")
    private String lastName;

    @NotNull(message = "El número de teléfono es obligatorio.")
    private BigInteger phoneNumber;

    @NotBlank(message = "El año de nacimiento es obligatorio.")
    @Size(max = 255, message ="El año de nacimiento no puede superar los 255 caracteres.")
    private String birthDate;

    @NotBlank(message = "La contraseña es obligatoria.")
    @Size(max = 255, message ="La contraseña no puede superar los 255 caracteres.")
    private String password;

    @NotBlank(message = "La dirección es obligatoria.")
    @Size(max = 50, message ="La dirección no puede superar los 50 caracteres.")
    private String address;

    @NotNull(message = "El DNI es obligatorio.")
    private BigInteger dni;

    @NotBlank(message = "La ciudad es obligatoria.")
    @Size(max = 50, message ="La ciudad no puede superar los 50 caracteres.")
    private String city;

    @NotBlank(message = "La provincia es obligatoria.")
    @Size(max = 50, message ="La provincia no puede superar los 50 caracteres.")
    private String province;

    @NotBlank(message = "El código postal es obligatorio.")
    @Size(max = 15, message ="El código postal no puede superar los 15 caracteres.")
    private String postalCode;

    @NotBlank(message = "El género es obligatorio.")
    @Size(max = 30, message ="El género no puede superar los 30 caracteres.")
    private String gender;

    @NotBlank(message = "El estado de empleo es obligatorio.")
    @Size(max = 30, message ="El empleo no puede superar los 30 caracteres.")
    private String employment;

}
