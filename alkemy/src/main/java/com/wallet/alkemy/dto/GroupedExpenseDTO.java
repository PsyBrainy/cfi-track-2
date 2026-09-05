package com.wallet.alkemy.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GroupedExpenseDTO {

    @com.fasterxml.jackson.annotation.JsonProperty("tipoMovimiento")
    private String movementType;
    private double total;
}
