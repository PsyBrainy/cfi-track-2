// js/tokenCheck.js
import { BaseUrl } from './config.js';

const URL_CHECK_SESSION = `${BaseUrl}/api/auth/check-session`;

(async function verificarRutaProtegida() {
    const token = localStorage.getItem('token');

    // 1. Validación de existencia del token local
    if (!token) {
        window.location.href = 'index.html';
        return;
    }

    // 2. Validación de vigencia y autenticación contra Spring Boot (Para todos por igual)
    try {
        const response = await fetch(URL_CHECK_SESSION, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        // Almacenar extensión de sesión si tu filtro emite el Refresh-Token
        const tokenExtendido = response.headers.get('Refresh-Token');
        if (tokenExtendido) {
            localStorage.setItem('token', tokenExtendido);
        }

        // Si el token es inválido o expiró
        if (response.status === 401 || response.status === 403) {
            console.warn('Sesión inválida o expirada en el servidor');
            localStorage.removeItem('token'); 
            window.location.href = 'index.html';
            return;
        }

        if (!response.ok) throw new Error(`Error: ${response.status}`);

        // =========================================================================
        // Sacar a los administradores de vistas de usuario
        // =========================================================================
        try {
            const partesToken = token.split('.');
            if (partesToken.length === 3) {
                const payloadRaw = partesToken[1];
                const base64 = payloadRaw.replace(/-/g, '+').replace(/_/g, '/');
                
                // Decodificación segura tolerante a formatos de Spring Boot
                const jsonPayload = decodeURIComponent(window.atob(base64).split('').map(function(c) {
                    return '%' + ('0' + c.charCodeAt(0).toString(16)).slice(-2);
                }).join(''));
                
                const payloadDecoded = JSON.parse(jsonPayload);

                // Comprobamos si las autoridades incluyen el rol de Administrador
                const esAdmin = payloadDecoded.authorities && payloadDecoded.authorities.includes('ROLE_ADMIN');

                if (esAdmin) {
                    window.location.href = 'admin-dashboard.html'; // Lo enviamos a su panel correspondiente
                    return;
                }
            }
        } catch (e) {
            localStorage.removeItem('token');
            window.location.href = 'index.html';
            return;
        }
        // =========================================================================

        // TODO CORRECTO: El token sirve y pertenece a un cliente estándar. Mostramos el panel.
        document.body.style.display = 'block';

    } catch (error) {
        console.error('Error de conexión con la API:', error);
        // Si el servidor se cae, protegemos la vista redirigiendo al index
        window.location.href = 'index.html';
    }
})();
