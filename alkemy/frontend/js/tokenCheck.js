// js/tokenCheck.js
import { BaseUrl } from './config.js';

const URL_BALANCE = `${BaseUrl}/api/account/balance`;

// Ejecución inmediata para actuar como guardián de ruta antes de que cargue el HTML
(async function verificarRutaProtegida() {

    const token = localStorage.getItem('token');

    if (!token) {
        window.location.href = 'index.html';
        return;
    }

    try {
        const response = await fetch(URL_BALANCE, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        // =========================================================================
        // REFRESO DE FRONTEND: Guarda el token extendido que devolvió Spring Boot
        // =========================================================================
        const tokenExtendido = response.headers.get('Refresh-Token');
        if (tokenExtendido) {
            localStorage.setItem('token', tokenExtendido);
            console.log('🔄 Sesión extendida 10 minutos más por actividad.');
        }
        // =========================================================================

        if (response.status === 401 || response.status === 403) {
            console.warn('Sesión expirada');
            localStorage.removeItem('token'); 
            window.location.href = 'index.html';
            return;
        }

        if (!response.ok) {
            throw new Error(`Error: ${response.status}`);
        }

        const data = await response.json();
        console.log('balance cargado con éxito:', data);

        // 🛡️ ¡NUEVA LÍNEA CLAVE!: Si todo está correcto, mostramos la pantalla al usuario
        document.body.style.display = 'block';

    } catch (error) {
        console.error('Hubo un problema al conectar con el servidor:', error);
        // Si el servidor se cae, también lo mandamos al index por seguridad
        window.location.href = 'index.html';
    }
})();
