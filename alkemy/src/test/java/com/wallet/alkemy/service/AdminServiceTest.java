package com.wallet.alkemy.service;



import com.wallet.alkemy.dto.AdminUserResponseDTO;
import com.wallet.alkemy.exception.UserNotFoundException;
import com.wallet.alkemy.models.tableBankAccount;
import com.wallet.alkemy.models.tableTransaction;
import com.wallet.alkemy.models.tableUser;
import com.wallet.alkemy.repository.AccountRepository;
import com.wallet.alkemy.repository.TransactionRepository;
import com.wallet.alkemy.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private AdminService adminService;

    private tableUser usuarioBase;
    private tableBankAccount cuentaBase;
    private tableTransaction transaccionBase;

    @BeforeEach
    void setUp() {
        usuarioBase = new tableUser();
        usuarioBase.setId(1L);
        usuarioBase.setName("Carla");
        usuarioBase.setLastName("Beron");
        usuarioBase.setEmail("carla@example.com");
        usuarioBase.setDni(new BigInteger("12345678"));
        usuarioBase.setPhoneNumber(new BigInteger("1122334455"));
        usuarioBase.setActive(true);
        usuarioBase.setDateCreated(LocalDate.from(LocalDateTime.now()));

        cuentaBase = new tableBankAccount();
        cuentaBase.setId(10L);
        cuentaBase.setIdUser(BigInteger.valueOf(1L));
        cuentaBase.setBalance(1500.0);
        cuentaBase.setCurrency("ARS");

        transaccionBase = new tableTransaction();
        transaccionBase.setBalance(2500.0);
        transaccionBase.setDateTransaction(LocalDateTime.now());
    }

    @Nested
    @DisplayName("Pruebas de Búsqueda y Lectura (obtenerTodosLosUsuarios / obtenerUsuarioPorId)")
    class LecturaTests {

        @Test
        @DisplayName("obtenerTodosLosUsuarios con filtro de email invoca findByEmailContainingIgnoreCase")
        void obtenerTodosLosUsuarios_ConEmail_FiltraCorrectamente() {
            when(userRepository.findByEmailContainingIgnoreCase("carla"))
                    .thenReturn(List.of(usuarioBase));

            List<AdminUserResponseDTO> resultado = adminService.obtenerTodosLosUsuarios("carla");

            assertNotNull(resultado);
            assertEquals(1, resultado.size());
            assertEquals("carla@example.com", resultado.get(0).getEmail());
            verify(userRepository, times(1)).findByEmailContainingIgnoreCase("carla");
            verify(userRepository, never()).findAll();
        }

        @Test
        @DisplayName("obtenerTodosLosUsuarios sin filtro (null o en blanco) invoca findAll")
        void obtenerTodosLosUsuarios_SinEmail_DevuelveTodos() {
            when(userRepository.findAll()).thenReturn(List.of(usuarioBase));

            List<AdminUserResponseDTO> resultado = adminService.obtenerTodosLosUsuarios("   ");

            assertNotNull(resultado);
            assertEquals(1, resultado.size());
            verify(userRepository, times(1)).findAll();
            verify(userRepository, never()).findByEmailContainingIgnoreCase(any());
        }

        @Test
        @DisplayName("obtenerUsuarioPorId mapea correctamente datos personales y billetera con transacción")
        void obtenerUsuarioPorId_Exitoso_ConTransaccion() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(usuarioBase));
            when(accountRepository.findByIdUser(BigInteger.valueOf(1L))).thenReturn(Optional.of(cuentaBase));
            when(transactionRepository.findFirstByAccountNumberOrderByIdDesc(BigInteger.valueOf(10L)))
                    .thenReturn(Optional.of(transaccionBase));

            AdminUserResponseDTO dto = adminService.obtenerUsuarioPorId(1L);

            assertNotNull(dto);
            assertEquals(1L, dto.getId());
            assertEquals("Carla Beron", dto.getNombre());
            assertEquals("ALK10", dto.getCvu());
            assertNotNull(dto.getBilletera());
            assertEquals(2500.0, dto.getBilletera().getSaldo()); // Saldo de la transacción
            verify(userRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("obtenerUsuarioPorId mapea saldo base si el usuario no tiene transacciones")
        void obtenerUsuarioPorId_Exitoso_SinTransaccionesUsaSaldoCuenta() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(usuarioBase));
            when(accountRepository.findByIdUser(BigInteger.valueOf(1L))).thenReturn(Optional.of(cuentaBase));
            when(transactionRepository.findFirstByAccountNumberOrderByIdDesc(BigInteger.valueOf(10L)))
                    .thenReturn(Optional.empty());

            AdminUserResponseDTO dto = adminService.obtenerUsuarioPorId(1L);

            assertNotNull(dto.getBilletera());
            assertEquals(1500.0, dto.getBilletera().getSaldo()); // Saldo por defecto de la cuenta
        }

        @Test
        @DisplayName("obtenerUsuarioPorId lanza UserNotFoundException si el ID no existe")
        void obtenerUsuarioPorId_UsuarioNoExiste_LanzaExcepcion() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class, () -> adminService.obtenerUsuarioPorId(99L));
            verifyNoInteractions(accountRepository, transactionRepository);
        }
    }

    @Nested
    @DisplayName("Pruebas de Modificación de Estado (actualizarEstadoLogico)")
    class EstadoLogicoTests {

        @Test
        @DisplayName("actualizarEstadoLogico modifica la propiedad 'active' a false")
        void actualizarEstadoLogico_DesactivarUsuario_Exitoso() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(usuarioBase));

            adminService.actualizarEstadoLogico(1L, false);

            assertFalse(usuarioBase.isActive());
            verify(userRepository, times(1)).save(usuarioBase);
        }

        @Test
        @DisplayName("actualizarEstadoLogico lanza UserNotFoundException si no existe el ID")
        void actualizarEstadoLogico_UsuarioNoExiste_LanzaExcepcion() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class, () -> adminService.actualizarEstadoLogico(99L, true));
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Pruebas de Actualización Completa (actualizarUsuarioCompleto)")
    class ActualizacionCompletaTests {

        @Test
        @DisplayName("actualizarUsuarioCompleto procesa nombre compuesto, DNI y teléfono con formato")
        void actualizarUsuarioCompleto_ConFormatosYNombreCompuesto() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(usuarioBase));

            AdminUserResponseDTO dto = new AdminUserResponseDTO();
            dto.setNombre("Carla Andrea");
            dto.setEmail("carla.nueva@example.com");
            dto.setActive(true);
            dto.setDni("40.123.456");      // Regex \D removerá puntos
            dto.setTelefono("+54 9 11 2233"); // Regex \D removerá espacios y signos

            adminService.actualizarUsuarioCompleto(1L, dto);

            assertEquals("Carla", usuarioBase.getName());
            assertEquals("Andrea", usuarioBase.getLastName());
            assertEquals("carla.nueva@example.com", usuarioBase.getEmail());
            assertEquals(new BigInteger("40123456"), usuarioBase.getDni());
            assertEquals(new BigInteger("549112233"), usuarioBase.getPhoneNumber());

            verify(userRepository, times(1)).save(usuarioBase);
        }

        @Test
        @DisplayName("actualizarUsuarioCompleto maneja un solo nombre asignando apellido en blanco")
        void actualizarUsuarioCompleto_NombreUnico() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(usuarioBase));

            AdminUserResponseDTO dto = new AdminUserResponseDTO();
            dto.setNombre("Carla");
            dto.setEmail("carla@example.com");

            adminService.actualizarUsuarioCompleto(1L, dto);

            assertEquals("Carla", usuarioBase.getName());
            assertEquals("", usuarioBase.getLastName());
        }

        @Test
        @DisplayName("actualizarUsuarioCompleto no falla con DNI y teléfono nulos o vacíos")
        void actualizarUsuarioCompleto_CamposOpcionalesNulos() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(usuarioBase));

            AdminUserResponseDTO dto = new AdminUserResponseDTO();
            dto.setNombre("Carla");
            dto.setDni(null);
            dto.setTelefono("   ");

            assertDoesNotThrow(() -> adminService.actualizarUsuarioCompleto(1L, dto));
            verify(userRepository, times(1)).save(usuarioBase);
        }

        @Test
        @DisplayName("actualizarUsuarioCompleto lanza UserNotFoundException cuando el ID no existe")
        void actualizarUsuarioCompleto_UsuarioNoExiste_LanzaExcepcion() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            AdminUserResponseDTO dto = new AdminUserResponseDTO();

            assertThrows(UserNotFoundException.class, () -> adminService.actualizarUsuarioCompleto(99L, dto));
            verify(userRepository, never()).save(any());
        }
    }
}