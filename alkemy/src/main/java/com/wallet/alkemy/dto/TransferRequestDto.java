package com.wallet.alkemy.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TransferRequestDto {

    private Long destinationAccountId;
    private Double amount;
    
    public TransferRequestDto(Long destinationAccountId, Double amount) {
        this.destinationAccountId = destinationAccountId;
        this.amount = amount;
    }
}