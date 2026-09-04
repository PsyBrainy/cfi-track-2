package com.wallet.alkemy.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TransferRequestDTO {

    @NotNull
    @com.fasterxml.jackson.annotation.JsonProperty("cuentaDestinoId")
    private Long destinationAccountId;

    @NotNull
    @Positive
    @com.fasterxml.jackson.annotation.JsonProperty("monto")
    private Double amount;
}
