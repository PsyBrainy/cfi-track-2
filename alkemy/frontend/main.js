// main.js — punto de entrada de la página principal (index.html).
// Importa los módulos de interfaz y coordina el estado de sesión.

import { initNav } from './js/nav.js';
import { actualizarNavSegunSesion } from './js/ui.js';
import { isLogged, clearToken } from './js/authState.js';

document.addEventListener('DOMContentLoaded', () => {
  initNav();

  const logueado = actualizarNavSegunSesion();

  // Botón "Cerrar sesión" del nav
  const logoutLink = document.getElementById('nav-logout-link');
  if (logoutLink) {
    logoutLink.addEventListener('click', (event) => {
      event.preventDefault();
      clearToken();
      window.location.href = 'index.html';
    });
  }

  // Botón "Empezar ahora": si está logueado va al dashboard, si no al registro
  const startButton = document.getElementById('start-button');
  if (startButton) {
    startButton.addEventListener('click', () => {
      window.location.href = logueado ? 'login.html' : 'registro.html';
    });
  }
});
