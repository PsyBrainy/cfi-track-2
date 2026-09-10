// login.js — lógica de la página de LOGIN.
import { BaseUrl } from './config.js';
import { setToken } from './authState.js';

export function initLogin() {
  const urlParams = new URLSearchParams(window.location.search);

  // =========================================================================
  // GUARDIÁN AFK - Captura si el usuario viene expulsado por inactividad
  // =========================================================================
  if (urlParams.get('motivo') === 'expirado') {
    Swal.fire({
      icon: 'warning',
      title: 'Sesión Expirada',
      text: 'Tu sesión se ha cerrado automáticamente por seguridad debido a la inactividad. Por favor, ingresa nuevamente.',
      confirmButtonText: 'Aceptar',
      confirmButtonColor: '#2563eb'
    }).then(() => {
      window.history.replaceState({}, document.title, window.location.pathname);
    });
  }

  // =========================================================================
  // CAPTURA DE RESULTADO DE ACTIVACIÓN DESDE EL EMAIL (ALKYWALL)
  // =========================================================================
  if (urlParams.get('activado')) {
    const estadoActivacion = urlParams.get('activado');

    if (estadoActivacion === 'exito') {
      Swal.fire({
        icon: 'success',
        title: '¡Cuenta Activada!',
        text: 'Tu correo electrónico ha sido verificado con éxito. Ya podés ingresar a tu billetera Alkywall y mover tu dinero.',
        confirmButtonText: 'Comenzar',
        confirmButtonColor: '#0F766E'
      }).then(() => {
        window.history.replaceState({}, document.title, window.location.pathname);
      });

    } else if (estadoActivacion === 'ya_activado' || estadoActivacion === 'ya-activado') {
      Swal.fire({
        icon: 'info',
        title: 'Cuenta ya Verificada',
        text: 'Esta cuenta ya había sido activada anteriormente. Podés iniciar sesión de forma normal.',
        confirmButtonText: 'Entendido',
        confirmButtonColor: '#111C3A'
      }).then(() => {
        window.history.replaceState({}, document.title, window.location.pathname);
      });

    } else if (estadoActivacion === 'error') {
      Swal.fire({
        icon: 'error',
        title: 'Enlace Expirado o Inválido',
        text: 'El token de verificación ha caducado (vence a las 24 horas) o es incorrecto. Por favor, registrate de nuevo o contactá a soporte.',
        confirmButtonText: 'Aceptar',
        confirmButtonColor: '#EF4444'
      }).then(() => {
        window.history.replaceState({}, document.title, window.location.pathname);
      });
    }
  }
  // =========================================================================

  const loginForm = document.getElementById('login-form');
  if (!loginForm) return;

  const loginEmailInput = document.getElementById('login-email');
  const loginPasswordInput = document.getElementById('login-password');
  const loginFeedback = document.getElementById('login-feedback');

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

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
          localStorage.clear();
          setToken(data.token);
          localStorage.setItem('token', data.token);
          
          try {
            const partesToken = data.token.split('.');
            if (partesToken.length === 3) {
              const payloadRaw = partesToken[1];
              const base64 = payloadRaw.replace(/-/g, '+').replace(/_/g, '/');
              const jsonPayload = decodeURIComponent(window.atob(base64).split('').map(function(c) {
                  return '%' + ('0' + c.charCodeAt(0).toString(16)).slice(-2);
              }).join(''));
              
              const payloadDecoded = JSON.parse(jsonPayload);
              const authorities = payloadDecoded.authorities || [];
              const esAdmin = authorities.includes('ADMIN') || authorities.includes('ROLE_ADMIN');
              
              if (esAdmin) {
                window.location.replace('admin-dashboard.html');
                return;
              }
            }
          } catch (error) {
            console.error('Error procesando el rol del token tras login:', error);
          }
          window.location.replace('dashboard.html');
        });
      }

      return response.json().then((errorData) => {
        if (errorData && errorData.message) {
          throw new Error(errorData.message);
        } else {
          throw new Error('Email o contraseña incorrectos.');
        }
      });
    })
    .catch((error) => {
      if (error && error.message && error.message !== "Failed to fetch") {
        mostrarFeedback(error.message);
      } else {
        mostrarFeedback('No se pudo conectar con el servidor.');
      }
    });
  });
}
