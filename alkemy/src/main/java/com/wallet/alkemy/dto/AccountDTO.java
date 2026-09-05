package com.wallet.alkemy.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountDTO {

    @com.fasterxml.jackson.annotation.JsonProperty("saldoDisponible")
    private double availableBalance;

    @com.fasterxml.jackson.annotation.JsonProperty("moneda")
    private String currency;
}
