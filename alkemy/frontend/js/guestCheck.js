// js/guestCheck.js

(function verificarRutaInvitado() {
    const token = localStorage.getItem('token');

    // Si ya existe un token, redirige de inmediato al inicio/dashboard
    if (token) {
        console.log("Usuario ya autenticado. Redirigiendo a la billetera...");
        window.location.href = './dashboard.html'; 
        // Si prefieres mandarlo directo a depósito, cambia por: './deposit.html'
    }
})();
