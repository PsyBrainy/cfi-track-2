/**
 * admin-user-detail.js
 * Gestión del expediente individual de usuario y actualización en el backend de Alkywall
 */

const API_BASE_URL = 'http://localhost:8080/api/admin';
let idUsuarioActual = null;

document.addEventListener('DOMContentLoaded', () => {
    // 1. Extraer el ID del usuario desde la URL (?id=...)
    const urlParams = new URLSearchParams(window.location.search);
    idUsuarioActual = urlParams.get('id');

    if (!idUsuarioActual) {
        window.location.href = 'admin-dashboard.html';
        return;
    }

    // 2. Cargar los datos del usuario desde el servidor
    cargarDetallesUsuario();

    // 3. Configurar el evento del formulario para guardar los cambios
    const formulario = document.getElementById('editUserForm');
    if (formulario) {
        formulario.addEventListener('submit', guardarCambiosUsuario);
    }
});

/**
 * Trae toda la información del usuario de la billetera virtual desde el backend
 */
async function cargarDetallesUsuario() {
    try {
        const token = localStorage.getItem('admin_token'); // Tu token JWT de admin
        
        const response = await fetch(`${API_BASE_URL}/users/${idUsuarioActual}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            }
        });

        if (!response.ok) {
            throw new Error('No se pudo obtener la información del usuario.');
        }

        const usuario = await response.json();

        // Mapear los datos personales al formulario de edición
        document.getElementById('header-user-name').textContent = usuario.nombre || 'Sin nombre';
        document.getElementById('user-name').value = usuario.nombre || '';
        document.getElementById('user-dni').value = usuario.dni || '';
        document.getElementById('user-email').value = usuario.email || '';
        document.getElementById('user-phone').value = usuario.telefono || '';
        
        // Mapear el estado booleano (Baja lógica) al selector select
        document.getElementById('user-status').value = usuario.active.toString();

        // Mapear los datos financieros de la Billetera Virtual (Panel derecho de lectura)
        if (usuario.billetera) {
            document.getElementById('wallet-balance').textContent = formatearMoneda(usuario.billetera.saldo);
            document.getElementById('wallet-type').textContent = usuario.billetera.tipoCuenta || 'Única Corriente';
            document.getElementById('wallet-cvu').textContent = usuario.billetera.cvu || 'No asignado';
            document.getElementById('wallet-updated').textContent = formatearFechaHora(usuario.billetera.updatedAt);
        }

    } catch (error) {

    }
}

/**
 * Envía la información modificada del usuario hacia el backend
 */
async function guardarCambiosUsuario(event) {
    event.preventDefault(); // Previene la recarga automática de la página

    // Capturar y estructurar los valores modificados del formulario
    const datosActualizados = {
        nombre: document.getElementById('user-name').value.trim(),
        dni: document.getElementById('user-dni').value.trim(),
        email: document.getElementById('user-email').value.trim(),
        telefono: document.getElementById('user-phone').value.trim(),
        // Convertimos el string "true"/"false" del select de nuevo a un Booleano puro
        active: document.getElementById('user-status').value === 'true'
    };

    try {
        const token = localStorage.getItem('admin_token');

        // Petición PUT o PATCH al endpoint de actualización del usuario
        const response = await fetch(`${API_BASE_URL}/users/${idUsuarioActual}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(datosActualizados)
        });

        if (!response.ok) {
            throw new Error('Error al actualizar los datos en el servidor.');
        }
        // Redirecciona de vuelta al dashboard general de administración
        window.location.href = 'admin-dashboard.html';

    } catch (error) {
    }
}

function formatearMoneda(monto) {
    if (monto === undefined || monto === null) return '$ 0,00';
    return new Intl.NumberFormat('es-AR', {
        style: 'currency',
        currency: 'ARS'
    }).format(monto);
}

function formatearFechaHora(fechaString) {
    if (!fechaString) return '-';
    const fecha = new Date(fechaString);
    
    const opcionesFecha = { day: '2-digit', month: 'short', year: 'numeric' };
    const opcionesHora = { hour: '2-digit', minute: '2-digit' };
    
    const f = fecha.toLocaleDateString('es-AR', opcionesFecha).replace('.', '');
    const h = fecha.toLocaleTimeString('es-AR', opcionesHora);
    
    return `${f} - ${h} hs`;
}
