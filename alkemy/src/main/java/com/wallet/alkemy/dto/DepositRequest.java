package com.wallet.alkemy.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = false)
public class DepositRequest {

    @NotNull
    @Positive
    @com.fasterxml.jackson.annotation.JsonProperty("monto")
    private Double amount;
}
