// login.js — lógica de la página de LOGIN.
// Al ingresar guarda el token en localStorage y redirige al index,
// que ya se muestra en modo "logueado" (oculta registro/login, muestra nav).

import { BaseUrl } from './config.js';
import { setToken } from './authState.js';

export function initLogin() {
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
            setToken(data.token);
            console.log('Login exitoso, token guardado en localStorage');
            // Redirige al index, que se mostrará en modo logueado
            window.location.href = 'index.html';
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
