package com.wallet.alkemy.dto;

import com.wallet.alkemy.enums.MovementType;
import com.wallet.alkemy.enums.TransactionType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {

    private MovementType movementType;
    private TransactionType type;
}
