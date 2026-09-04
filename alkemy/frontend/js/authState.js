// authState.js — utilidades compartidas para manejar el estado de sesión.
// Se puede importar desde cualquier página (index, login, registro, dashboard).

const TOKEN_KEY = 'token';

export function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

export function isLogged() {
  return Boolean(getToken());
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token);
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY);
}
