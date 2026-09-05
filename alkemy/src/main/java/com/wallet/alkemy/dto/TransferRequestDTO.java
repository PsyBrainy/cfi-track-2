package com.wallet.alkemy.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TransferRequestDTO {

    @NotBlank(message = "El correo electrónico del destinatario es obligatorio.")
    @Email(message = "El formato del correo electrónico del destinatario no es válido.")
    @com.fasterxml.jackson.annotation.JsonProperty("emailDestino")
    private String destinationEmail;

    @NotNull
    @Positive
    @com.fasterxml.jackson.annotation.JsonProperty("monto")
    private Double amount;
}
