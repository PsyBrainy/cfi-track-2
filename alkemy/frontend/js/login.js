// login.js — lógica de la página de LOGIN.
import { BaseUrl } from './config.js';
import { setToken } from './authState.js';

export function initLogin() {
  // =========================================================================
  // NUEVO: GUARDIÁN AFK - Captura si el usuario viene expulsado por inactividad
  // =========================================================================
  const urlParams = new URLSearchParams(window.location.search);
  if (urlParams.get('motivo') === 'expirado') {
    Swal.fire({
      icon: 'warning',
      title: 'Sesión Expirada',
      text: 'Tu sesión se ha cerrado automáticamente por seguridad debido a la inactividad. Por favor, ingresa nuevamente.',
      confirmButtonText: 'Aceptar',
      confirmButtonColor: '#2563eb'
    }).then(() => {
      // Limpia el parámetro de la URL (?motivo=expirado) para evitar duplicados
      window.history.replaceState({}, document.title, window.location.pathname);
    });
  }
  // =========================================================================

  const loginForm = document.getElementById('login-form');
  if (!loginForm) return;

  const loginEmailInput = document.getElementById('login-email');
  const loginPasswordInput = document.getElementById('login-password');
  const loginFeedback = document.getElementById('login-feedback');

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

  // Si venimos de un registro exitoso, precargamos el email (?email=)
  const params = new URLSearchParams(window.location.search);
  const emailParam = params.get('email');
  if (emailParam && loginEmailInput) {
    loginEmailInput.value = emailParam;
  }

  function mostrarErrorLogin(input, mensaje) {
    const errorSpan = document.getElementById(`${input.id}-error`);
    if (errorSpan) errorSpan.textContent = mensaje;
    input.classList.add('input--error');
  }

  function limpiarErrorLogin(input) {
    const errorSpan = document.getElementById(`${input.id}-error`);
    if (errorSpan) errorSpan.textContent = '';
    input.classList.remove('input--error');
  }

  function mostrarFeedback(mensaje) {
    loginFeedback.textContent = mensaje;
    loginFeedback.classList.add('form-feedback--error');
    loginFeedback.classList.remove('form-feedback--success');
  }

  function limpiarFeedback() {
    loginFeedback.textContent = '';
    loginFeedback.classList.remove('form-feedback--error');
    loginFeedback.classList.remove('form-feedback--success');
  }

  loginForm.addEventListener('submit', (event) => {
    event.preventDefault();
    limpiarFeedback();

    let esValido = true;

    if (loginEmailInput.value.trim() === '') {
      mostrarErrorLogin(loginEmailInput, 'El email no puede estar vacío.');
      esValido = false;
    } else if (!emailRegex.test(loginEmailInput.value.trim())) {
      mostrarErrorLogin(loginEmailInput, 'Ingresá un email válido.');
      esValido = false;
    } else {
      limpiarErrorLogin(loginEmailInput);
    }

    if (loginPasswordInput.value === '') {
      mostrarErrorLogin(loginPasswordInput, 'La contraseña no puede estar vacía.');
      esValido = false;
    } else {
      limpiarErrorLogin(loginPasswordInput);
    }

    if (!esValido) return;

    fetch(`${BaseUrl}/api/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        email: loginEmailInput.value.trim(),
        password: loginPasswordInput.value
      })
    })
      .then((response) => {
        if (response.ok) {
          return response.json().then((data) => {
            // Limpieza absoluta de residuos de sesiones previas (como strings "null")
            localStorage.clear();

            // 1. Guardamos el token en el estado de la aplicación
            setToken(data.token);
            console.log('Login exitoso, token guardado en localStorage');

            // Guardamos adicionalmente la clave que usa el panel administrativo de forma explícita
            localStorage.setItem('admin_token', data.token);

            // =========================================================================
            // 🔄 REDIRECCIÓN INTELIGENTE BASADA EN ROLES (JWT) - CORREGIDA
            // =========================================================================
            try {
              const partesToken = data.token.split('.');
              if (partesToken.length === 3) {
                const payloadRaw = partesToken[1];
                const base64 = payloadRaw.replace(/-/g, '+').replace(/_/g, '/');
                
                const jsonPayload = decodeURIComponent(window.atob(base64).split('').map(function(c) {
                    return '%' + ('0' + c.charCodeAt(0).toString(16)).slice(-2);
                }).join(''));
                
                const payloadDecoded = JSON.parse(jsonPayload);

                // CORRECCIÓN: Buscamos tanto 'ADMIN' como 'ROLE_ADMIN' de forma tolerante
                const authorities = payloadDecoded.authorities || [];
                const esAdmin = authorities.includes('ADMIN') || authorities.includes('ROLE_ADMIN');

                if (esAdmin) {
                  console.log('🛡️ Administrador legítimo detectado. Direccionando al Panel de Control...');
                  window.location.replace('admin-dashboard.html'); // Evita acumular historial corrupto
                  return;
                }
              }
            } catch (error) {
              console.error('Error procesando el rol del token tras login:', error);
            }

            // Destino por defecto para usuarios comunes o fallas de lectura
            console.log('👥 Cliente estándar detectado. Direccionando a la billetera...');
            window.location.replace('dashboard.html');
            // =========================================================================
          });
        }

        if (response.status === 401 || response.status === 403) {
          mostrarFeedback('Email o contraseña incorrectos.');
          return;
        }
        mostrarFeedback('Ocurrió un error al iniciar sesión. Intentá de nuevo.');
      })
      .catch(() => {
        mostrarFeedback('No se pudo conectar con el servidor.');
      });
  });
}
