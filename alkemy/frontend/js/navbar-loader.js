// js/navbar-loader.js
import { BaseUrl } from './config.js';

const URL_BALANCE = `${BaseUrl}/api/account/balance`;
const FIVE_MINUTES = 5 * 60 * 1000; // 5 minutos para el AFK en milisegundos
let temporizadorInactividad;

const navbarContainer = document.getElementById("global-navbar");

if (navbarContainer) {
    try {
        // 1. Descargamos e inyectamos el HTML del navbar
        const responseHtml = await fetch("components/navbar.html");
        if (!responseHtml.ok) throw new Error("No se pudo cargar el navbar");

        const html = await responseHtml.text();
        navbarContainer.innerHTML = html;

        // 2. Inicializamos los eventos del menú móvil
        inicializarMenuMovil();

        // 3. Controlamos qué botones se muestran según el estado de la sesión
        await verificarEstadoSesion();

    } catch (error) {
        console.error("Error al inicializar el navbar global:", error);
    }
}

function inicializarMenuMovil() {
    const toggle = document.querySelector(".nav-toggle");
    const menu = document.querySelector("#nav-menu");
    if (toggle && menu) {
        toggle.addEventListener("click", () => {
            const expanded = toggle.getAttribute("aria-expanded") === "true";
            toggle.setAttribute("aria-expanded", !expanded);
            menu.classList.toggle("nav--visible");
        });
    }
}

async function verificarEstadoSesion() {
    const token = localStorage.getItem('token');

    const elementosPrivados = document.querySelectorAll('[data-auth-required]');
    const botonLogin = document.getElementById('login-link');
    const botonLogout = document.querySelector('[data-logout-link]');

    // SI NO HAY TOKEN (Usuario visitante / No registrado)
    if (!token) {
        elementosPrivados.forEach(el => {
            const li = el.closest('li');
            if (li) li.style.display = 'none';
        });
        if (botonLogout) {
            const liLogout = botonLogout.closest('li');
            if (liLogout) liLogout.style.display = 'none';
        }
        if (botonLogin) {
            const liLogin = botonLogin.closest('li');
            if (liLogin) liLogin.style.display = 'block';
        }
        return;
    }

    // SI HAY TOKEN, verificamos si sigue siendo válido contra el Backend
    try {
        const response = await fetch(URL_BALANCE, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        // REFRESO DE FRONTEND: Guarda el token extendido que devolvió Spring Boot (Sliding Expiration)
        const tokenExtendido = response.headers.get('Refresh-Token');
        if (tokenExtendido) {
            localStorage.setItem('token', tokenExtendido);
            console.log('🔄 Sesión extendida por actividad.');
        }

        // CONTROL DE EXPIRACIÓN / NO AUTORIZADO
        if (response.status === 401 || response.status === 403) {
            console.warn('Sesión expirada');
            localStorage.removeItem('token');

            const paginasPublicas = ['index.html', 'login.html', 'registro.html', ''];
            const paginaActual = window.location.pathname.split('/').pop();

            if (!paginasPublicas.includes(paginaActual)) {
                window.location.href = './login.html?motivo=expirado';
            } else {
                elementosPrivados.forEach(el => {
                    const li = el.closest('li');
                    if (li) li.style.display = 'none';
                });
                if (botonLogout) {
                    const liLogout = botonLogout.closest('li');
                    if (liLogout) liLogout.style.display = 'none';
                }
                if (botonLogin) {
                    const liLogin = botonLogin.closest('li');
                    if (liLogin) liLogin.style.display = 'block';
                }
            }
            return;
        }

        if (!response.ok) throw new Error(`Error: ${response.status}`);

        // =========================================================================
        // TOKEN VÁLIDO: Ocultamos y mostramos los elementos contenedores completos (li)
        // =========================================================================
        elementosPrivados.forEach(el => {
            const li = el.closest('li');
            if (li) li.style.display = 'block';
        });

        if (botonLogout) {
            const liLogout = botonLogout.closest('li');
            if (liLogout) liLogout.style.display = 'block';
            
            botonLogout.onclick = () => {
                localStorage.removeItem('token');
                window.location.href = 'index.html';
            };
        }

        // Ocultamos Iniciar Sesión por completo
        if (botonLogin) {
            const liLogin = botonLogin.closest('li');
            if (liLogin) liLogin.style.display = 'none';
        }

        // -------------------------------------------------------------------------
        // MUTACIÓN DE RUTAS PÚBLICAS: Evitamos que regrese a la vista de Landing
        // -------------------------------------------------------------------------
        const logoLink = document.getElementById('nav-logo');
        const inicioLink = document.getElementById('nav-inicio');

        // Modificamos el link del Logo de la marca para que apunte al Dashboard
        if (logoLink) {
            logoLink.href = 'deposit.html';
        }

        // Ocultamos el elemento de lista de la opción "Inicio" del menú
        if (inicioLink) {
            const liInicio = inicioLink.closest('li');
            if (liInicio) liInicio.style.display = 'none';
        }
        // -------------------------------------------------------------------------

        // Iniciamos el temporizador de inactividad física (AFK)
        activarMonitoreoInactividad();

    } catch (error) {
        console.error('Error al validar sesión en el navbar:', error);
    }
}

function activarMonitoreoInactividad() {
    reiniciarContador();

    window.addEventListener('mousemove', reiniciarContador);
    window.addEventListener('mousedown', reiniciarContador);
    window.addEventListener('keydown', reiniciarContador);
    window.addEventListener('click', reiniciarContador);
}

function reiniciarContador() {
    clearTimeout(temporizadorInactividad);
    temporizadorInactividad = setTimeout(() => {
        if (localStorage.getItem('token')) {
            localStorage.removeItem('token');
            window.location.href = './login.html?motivo=expirado';
        }
    }, FIVE_MINUTES);
}
