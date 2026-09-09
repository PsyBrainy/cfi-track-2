package com.wallet.alkemy.controllers;
import com.wallet.alkemy.service.AdminService;
import com.wallet.alkemy.dto.AdminUserResponseDTO;
import com.wallet.alkemy.dto.UserStatusRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Permite peticiones de tu Frontend local sin bloqueos de CORS
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<List<AdminUserResponseDTO>> listarUsuarios(@RequestParam(value = "email", required = false) String email) {
        return ResponseEntity.ok(adminService.obtenerTodosLosUsuarios(email));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<AdminUserResponseDTO> obtenerDetalleUsuario(@PathVariable("id") Long id) {
        return ResponseEntity.ok(adminService.obtenerUsuarioPorId(id));
    }

    @PatchMapping("/users/{id}/status")
    public ResponseEntity<Void> cambiarEstado(@PathVariable("id") Long id, @RequestBody UserStatusRequestDTO statusRequest) {
        adminService.actualizarEstadoLogico(id, statusRequest.isActive());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<Void> actualizarUsuario(@PathVariable("id") Long id, @RequestBody AdminUserResponseDTO datosActualizados) {
        adminService.actualizarUsuarioCompleto(id, datosActualizados);
        return ResponseEntity.ok().build();
    }
}
