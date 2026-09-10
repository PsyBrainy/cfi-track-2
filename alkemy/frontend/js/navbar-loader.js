import { BaseUrl } from './config.js';

const URL_CHECK_SESSION = `${BaseUrl}/api/auth/check-session`;
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

    try {
        // =========================================================================
        // DECODIFICACIÓN DEL ROL DE ADMINISTRADOR DESDE EL JWT
        // =========================================================================
        let esAdmin = false;
        try {
            const partesToken = token.split('.');
            if (partesToken.length === 3) {
                const payloadRaw = partesToken[1]; // Índice 1: Payload string
                const base64 = payloadRaw.replace(/-/g, '+').replace(/_/g, '/');
                const jsonPayload = decodeURIComponent(window.atob(base64).split('').map(function(c) {
                    return '%' + ('0' + c.charCodeAt(0).toString(16)).slice(-2);
                }).join(''));
                const payloadDecoded = JSON.parse(jsonPayload);
                
                const authorities = payloadDecoded.authorities || [];
                esAdmin = authorities.includes('ADMIN') || authorities.includes('ROLE_ADMIN');
            }
        } catch (e) {
            console.error("Error decodificando el rol en el Navbar:", e);
        }

        // VALIDACIÓN DE VIGENCIA CONTRA SPRING BOOT
        const response = await fetch(URL_CHECK_SESSION, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        const tokenExtendido = response.headers.get('Refresh-Token');
        if (tokenExtendido) {
            localStorage.setItem('token', tokenExtendido);
        }

        // CONTROL DE EXPIRACIÓN / NO AUTORIZADO
        if (response.status === 401 || response.status === 403) {
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
        // CONTROL DE VISIBILIDAD BASADO EN EL TEXTO DEL NAVBAR REAL
        // =========================================================================
        const todosLosEnlaces = document.querySelectorAll('#nav-menu a, .navbar-nav a, nav a');

        if (esAdmin) {
            // SI ES ADMIN: Buscamos "Inicio", "Mi billetera" y "Ayuda" para borrarlos del DOM
            todosLosEnlaces.forEach(enlace => {
                const texto = enlace.textContent.trim().toLowerCase();
                if (texto === 'inicio' || texto === 'mi billetera' || texto === 'ayuda') {
                    const liContenedor = enlace.closest('li');
                    if (liContenedor) {
                        liContenedor.style.display = 'none'; // Desaparece por completo
                    }
                }
            });

            // Cambiamos el comportamiento del logo para redirigir al panel de control admin
            const logoLink = document.getElementById('nav-logo') || document.querySelector('.nav-brand') || document.querySelector('.logo');
            if (logoLink) {
                logoLink.href = 'admin-dashboard.html';
            }

        } else {
            // SI ES USER COMÚN: Comportamiento inicial estándar
            elementosPrivados.forEach(el => {
                const li = el.closest('li');
                if (li) li.style.display = 'block'; // Asegura que vea "Mi billetera"
            });

            // Mutación para ocultar "Inicio" al loguearse como cliente regular
            todosLosEnlaces.forEach(enlace => {
                const texto = enlace.textContent.trim().toLowerCase();
                if (texto === 'inicio') {
                    const liContenedor = enlace.closest('li');
                    if (liContenedor) liContenedor.style.display = 'none';
                }
            });

            const logoLink = document.getElementById('nav-logo') || document.querySelector('.nav-brand') || document.querySelector('.logo');
            if (logoLink) {
                logoLink.href = 'deposit.html';
            }
        }

        // CONTROL EXCLUSIVO DEL BOTÓN CERRAR SESIÓN (Para ambos roles)
        if (botonLogout) {
            const liLogout = botonLogout.closest('li');
            if (liLogout) liLogout.style.display = 'block';
            
            botonLogout.onclick = () => {
                localStorage.clear(); // Limpia token y admin_token de una sola vez
                window.location.href = 'index.html';
            };
        }

        // Ocultamos Iniciar Sesión por completo
        if (botonLogin) {
            const liLogin = botonLogin.closest('li');
            if (liLogin) liLogin.style.display = 'none';
        }

        // Iniciamos el temporizador de inactividad física (AFK)
        activarMonitoreoInactividad();

    } catch (error) {
        console.error("Error controlando los roles en el navbar inyectado:", error);
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
