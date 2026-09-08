package com.wallet.alkemy.service;

import com.wallet.alkemy.dto.AdminUserResponseDTO;
import com.wallet.alkemy.dto.WalletInfoDTO;
import com.wallet.alkemy.exception.UserNotFoundException;
import com.wallet.alkemy.models.tableTransaction;
import com.wallet.alkemy.models.tableUser;
import com.wallet.alkemy.repository.AccountRepository;
import com.wallet.alkemy.repository.TransactionRepository;
import com.wallet.alkemy.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    // Constructor manual para asegurar la inyección limpia de Spring sin depender de Lombok
    public AdminService(UserRepository userRepository, 
                        AccountRepository accountRepository, 
                        TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminUserResponseDTO> obtenerTodosLosUsuarios(String email) {
        List<tableUser> usuarios;
        if (email != null && !email.isBlank()) {
            usuarios = userRepository.findByEmailContainingIgnoreCase(email); 
        } else {
            usuarios = userRepository.findAll();
        }
        return usuarios.stream().map(this::convertirADTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AdminUserResponseDTO obtenerUsuarioPorId(Long id) {
        tableUser user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));
        return convertirADTO(user);
    }

    @Transactional
    public void actualizarEstadoLogico(Long id, boolean active) {
        tableUser user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));
        user.setActive(active);
        userRepository.save(user);
    }

    @Transactional
    public void actualizarUsuarioCompleto(Long id, AdminUserResponseDTO dto) {
        tableUser user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));
        
        String nombreCompleto = dto.getNombre() != null ? dto.getNombre().trim() : "";
        int espacioIdx = nombreCompleto.indexOf(" ");
        if (espacioIdx != -1) {
            user.setName(nombreCompleto.substring(0, espacioIdx));
            user.setLastName(nombreCompleto.substring(espacioIdx + 1));
        } else {
            user.setName(nombreCompleto);
            user.setLastName("");
        }

        user.setEmail(dto.getEmail());
        user.setActive(dto.isActive());
        
        // Conversión segura de String a BigInteger
        if (dto.getDni() != null && !dto.getDni().trim().isBlank()) {
            user.setDni(new java.math.BigInteger(dto.getDni().trim().replaceAll("\\D", "")));
        }
        if (dto.getTelefono() != null && !dto.getTelefono().trim().isBlank()) {
            user.setPhoneNumber(new java.math.BigInteger(dto.getTelefono().trim().replaceAll("\\D", "")));
        }
        
        userRepository.save(user);
    }

    private AdminUserResponseDTO convertirADTO(tableUser user) {
        AdminUserResponseDTO dto = new AdminUserResponseDTO();
        dto.setId(user.getId());
        dto.setNombre(user.getName() + " " + (user.getLastName() != null ? user.getLastName() : "")); 
        dto.setEmail(user.getEmail());
        
        // Mapeo seguro de BigInteger a String para el Front
        dto.setDni(user.getDni() != null ? user.getDni().toString() : "");
        dto.setTelefono(user.getPhoneNumber() != null ? user.getPhoneNumber().toString() : "");
        dto.setActive(user.isActive());
        dto.setCreatedAt(user.getDateCreated());

        // Buscar cuenta usando BigInteger de ID de usuario
        accountRepository.findByIdUser(BigInteger.valueOf(user.getId())).ifPresent(account -> {
            dto.setCvu("ALK" + account.getId()); 
            
            double saldoActual = transactionRepository
                    .findFirstByAccountNumberOrderByIdDesc(BigInteger.valueOf(account.getId()))
                    .map(tableTransaction::getBalance)
                    .orElse(account.getBalance()); 

            java.time.LocalDateTime ultimaActualizacion = transactionRepository
                    .findFirstByAccountNumberOrderByIdDesc(BigInteger.valueOf(account.getId()))
                    .map(tableTransaction::getDateTransaction)
                    .orElse(java.time.LocalDateTime.now());

            WalletInfoDTO walletDTO = new WalletInfoDTO(
                    saldoActual,
                    "Cuenta Única (" + account.getCurrency() + ")",
                    dto.getCvu(),
                    ultimaActualizacion
            );
            dto.setBilletera(walletDTO);
        });

        return dto;
    }
}
