// ui.js — lógica de interfaz compartida.
// Actualiza la barra de navegación según si el usuario está logueado:
//   - Logueado:   muestra "Mi billetera" y "Movimientos", y "Cerrar sesión"
//   - Sin sesión: muestra "Iniciar sesión" y "Registrarse"

import { isLogged } from './authState.js';

export function actualizarNavSegunSesion() {
  const logueado = isLogged();

  // Links del nav que solo corresponden a usuarios logueados
  document.querySelectorAll('.nav-item--auth').forEach((item) => {
    item.style.display = logueado ? '' : 'none';
  });

  // Secciones de contenido que solo se muestran a usuarios logueados
  document.querySelectorAll('.section--auth').forEach((item) => {
    item.style.display = logueado ? '' : 'none';
  });

  // Secciones de contenido que solo se muestran a usuarios NO logueados
  document.querySelectorAll('.section--guest').forEach((item) => {
    item.style.display = logueado ? 'none' : '';
  });

  // Link "Iniciar sesión" (solo si NO está logueado)
  const loginItem = document.getElementById('nav-login-item');
  if (loginItem) {
    loginItem.style.display = logueado ? 'none' : '';
  }

  // Link "Registrarse" (solo si NO está logueado)
  const registroItem = document.getElementById('nav-registro-item');
  if (registroItem) {
    registroItem.style.display = logueado ? 'none' : '';
  }

  // Item "Cerrar sesión" (solo si está logueado)
  const logoutItem = document.getElementById('nav-logout-item');
  if (logoutItem) {
    logoutItem.style.display = logueado ? '' : 'none';
    return logueado;
  }

  return logueado;
}
