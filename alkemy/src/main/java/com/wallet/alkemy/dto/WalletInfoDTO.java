package com.wallet.alkemy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WalletInfoDTO {
    private Double saldo;
    private String tipoCuenta;
    private String cvu;
    private LocalDateTime updatedAt;
}
