package com.wallet.alkemy.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminUserResponseDTO {
    private Long id;
    private String nombre;
    private String email;
    private String dni;
    private String telefono;
    private String cvu;
    private boolean active;
    private LocalDate createdAt;
    private WalletInfoDTO billetera; // Para el panel de detalle derecho
}
