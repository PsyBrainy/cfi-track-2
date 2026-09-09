// js/adminTokenCheck.js
import { BaseUrl } from './config.js';

const URL_CHECK_SESSION = `${BaseUrl}/api/auth/check-session`;

(async function verificarRutaAdmin() {
    const token = localStorage.getItem('token');

    // 1. Si no hay token, al index directo
    if (!token) {
        window.location.href = 'index.html';
        return;
    }

    // 2. 🛡️ ESCUDO DE ROL EXCLUSIVO: Validamos localmente tu ROLE_ADMIN
    try {
        const partesToken = token.split('.');
        
        if (partesToken.length !== 3) {
            throw new Error('Formato de token inválido.');
        }

        // 🎯 CORRECCIÓN QUIRÚRGICA: Tomamos el índice [1] que es el Payload string
        const payloadRaw = partesToken[1]; 
        const base64 = payloadRaw.replace(/-/g, '+').replace(/_/g, '/');
        
        // Decodificación segura para el JSON de Spring Boot
        const jsonPayload = decodeURIComponent(window.atob(base64).split('').map(function(c) {
            return '%' + ('0' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join(''));
        
        const payloadDecoded = JSON.parse(jsonPayload);


        // Verificamos si tenés el rol de administrador
        const esAdmin = payloadDecoded.authorities && payloadDecoded.authorities.includes('ROLE_ADMIN');

        if (!esAdmin) {
            window.location.href = 'dashboard.html'; 
            return;
        }
        
    } catch (e) {
        console.error('Error crítico al decodificar y validar el JWT:', e);
        localStorage.removeItem('token');
        window.location.href = 'index.html';
        return;
    }

    // 3. VALIDACIÓN DE VIGENCIA CONTRA SPRING BOOT
    try {
        const response = await fetch(URL_CHECK_SESSION, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (response.status === 401 || response.status === 403) {
            console.warn('Sesión de administrador inválida o expirada en el servidor.');
            localStorage.removeItem('token'); 
            window.location.href = 'index.html';
            return;
        }

        if (!response.ok) throw new Error(`Error: ${response.status}`);

        // ¡ÉXITO TOTAL!: Sos admin y el token es válido
        document.body.style.display = 'block';

    } catch (error) {
        console.error('Error de conexión con la API:', error);
        window.location.href = 'index.html';
    }
})();
