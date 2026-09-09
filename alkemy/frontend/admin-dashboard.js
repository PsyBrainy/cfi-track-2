/**
 * admin-dashboard.js
 * Gestión de usuarios y comunicación con la API de Alkywall
 */

const API_BASE_URL = 'http://localhost:8080/api/admin'; 

// Variables globales de estado local
let usuarios = [];
let queryBusqueda = '';

// Al cargar el documento, inicializamos los componentes validando la sesión primero
document.addEventListener('DOMContentLoaded', () => {
    initDashboard();
});

async function initDashboard() {
    const token = localStorage.getItem('admin_token');

    // 1. Verificación local rápida para no hacer peticiones innecesarias si no hay sesión
    if (!token || token === "null" || token === "undefined") {
        window.location.href = 'login.html';
        return;
    }

    // 2. Consulta de validación al endpoint de revisión de sesión
    try {
        const response = await fetch('http://localhost:8080/api/auth/check-session', { 
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        // Si el filtro JWT del backend rebotó la petición (401 / 403), saltamos al catch
        if (!response.ok) {
            throw new Error('Sesión inválida o expirada.');
        }

        // 3. Si el estado es OK (200), el administrador es válido y cargamos el panel
        
        // Ejecutamos la carga de datos protegida
        obtenerUsuariosDeBackend();

        // Configurar el listener del filtro de búsqueda por email (Debounce)
        const emailFilterInput = document.getElementById('emailFilter');
        if (emailFilterInput) {
            emailFilterInput.addEventListener('keyup', debounce(() => {
                queryBusqueda = emailFilterInput.value.trim();
                obtenerUsuariosDeBackend(); // Consultar al backend con el filtro
            }, 300));
        }

    } catch (error) {
        console.error('Acceso denegado:', error);
        localStorage.removeItem('admin_token'); // Limpiamos el token inválido o expirado
        window.location.href = 'login.html'; // Redirección forzada de seguridad
    }
}

/**
 * Petición asíncrona para traer los usuarios desde el backend
 */
async function obtenerUsuariosDeBackend() {
    try {
        // Construimos la URL agregando el parámetro de búsqueda si existe
        let url = `${API_BASE_URL}/users`;
        if (queryBusqueda) {
            url += `?email=${encodeURIComponent(queryBusqueda)}`;
        }

        // Construcción segura de Headers para evitar enviar strings "null" corruptos
        const token = localStorage.getItem('admin_token'); 
        const headers = {
            'Content-Type': 'application/json'
        };

        if (token && token !== "null" && token !== "undefined") {
            headers['Authorization'] = `Bearer ${token}`;
        }
        
        const response = await fetch(url, {
            method: 'GET',
            headers: headers
        });

        if (!response.ok) {
            throw new Error('Error al responder desde el servidor.');
        }

        usuarios = await response.json();
        renderizarTablaUsuarios(usuarios);
        actualizarTarjetasMetricas(usuarios); // Actualiza los contadores superiores en tiempo real

    } catch (error) {
        console.error('Error cargando usuarios:', error);
        mostrarMensajeErrorTabla();
    }
}

/**
 * Renderiza dinámicamente las filas de la tabla protegiendo tus estilos
 */
function renderizarTablaUsuarios(listaUsuarios) {
    const tbody = document.querySelector('#usersTable tbody');
    if (!tbody) return;

    // Limpiamos las filas anteriores
    tbody.innerHTML = '';

    if (listaUsuarios.length === 0) {
        tbody.innerHTML = `
            <tr id="noResultsRow">
                <td colspan="5" class="text-center admin-empty-message">
                    No se encontraron usuarios que coincidan con el correo ingresado.
                </td>
            </tr>`;
        return;
    }

    listaUsuarios.forEach(usuario => {
        const fila = document.createElement('tr');
        fila.className = 'user-row';

        // Definimos la etiqueta visual del estado lógico (Boolean: active)
        const statusClass = usuario.active ? 'completed' : 'status-tag--inactive';
        const statusText = usuario.active ? 'Activo' : 'Inactivo';
        
        // El botón cambia de color y texto según el booleano actual
        const actionButton = usuario.active 
            ? `<button class="btn-admin-action btn-admin-action--disable" onclick="confirmarCambioEstado('${usuario.id}', false, '${usuario.nombre}')">Desactivar</button>`
            : `<button class="btn-admin-action btn-admin-action--enable" onclick="confirmarCambioEstado('${usuario.id}', true, '${usuario.nombre}')">Activar</button>`;

        fila.innerHTML = `
            <td class="user-name-cell">
                <strong class="admin-highlight-text">${usuario.nombre}</strong>
                <span class="admin-sub-cvu">CVU: ${usuario.cvu || 'No asignado'}</span>
            </td>
            <td class="user-email-cell">${usuario.email}</td>
            <td>${formatearFecha(usuario.createdAt)}</td>
            <td><span class="status-tag ${statusClass}">${statusText}</span></td>
            <td class="text-right">
                <div class="admin-actions">
                    <a href="admin-user-details.html?id=${usuario.id}" class="btn-admin-action btn-admin-action--edit">Gestionar</a>
                    ${actionButton}
                </div>
            </td>
        `;

        tbody.appendChild(fila);
    });
}

/**
 * @param {string} idUsuario - ID único del cliente
 * @param {boolean} nuevoEstado - El valor active (true/false) que se guardará
 * @param {string} nombre - Nombre para el mensaje de confirmación
 */
async function confirmarCambioEstado(idUsuario, nuevoEstado, nombre) {
    const accionText = nuevoEstado ? 'activar' : 'desactivar';
    const mensaje = `¿Estás seguro de que deseas ${accionText} la cuenta de ${nombre}?`;

    if (!confirm(mensaje)) return;

    try {
        const token = localStorage.getItem('admin_token');
        const headers = {
            'Content-Type': 'application/json'
        };

        if (token && token !== "null" && token !== "undefined") {
            headers['Authorization'] = `Bearer ${token}`;
        }
        
        // CORRECCIÓN ABSOLUTA: Construcción manual y limpia de la URL de administración
        const urlCompleta = `http://localhost:8080/api/admin/users/${idUsuario}/status`;
        const response = await fetch(urlCompleta, {
            method: 'PATCH',
            headers: headers,
            body: JSON.stringify({ active: nuevoEstado })
        });

        if (!response.ok) {
            throw new Error('El backend rechazó la actualización del estado.');
        }
        
        // Refrescamos los datos de la pantalla llamando de nuevo a la API
        obtenerUsuariosDeBackend();

    } catch (error) {
        console.error('Error al cambiar estado lógico:', error);
    }
}

/**
 * Calcula y renderiza los contadores de las tarjetas informativas superiores
 */
function actualizarTarjetasMetricas(listaUsuarios) {
    const totalCard = document.querySelector('.admin-dashboard-card:nth-child(1) h2, #totalUsersCount');
    const activeCard = document.querySelector('.admin-dashboard-card:nth-child(2) h2, #activeUsersCount');
    const inactiveCard = document.querySelector('.admin-dashboard-card:nth-child(3) h2, #inactiveUsersCount');

    const total = listaUsuarios.length;
    const activos = listaUsuarios.filter(u => u.active).length;
    const inactivos = total - activos;

    if (totalCard) totalCard.textContent = total;
    if (activeCard) activeCard.textContent = activos;
    if (inactiveCard) inactiveCard.textContent = inactivos;
}

/**
 * Muestra una advertencia visual si el servidor falla
 */
function mostrarMensajeErrorTabla() {
    const tbody = document.querySelector('#usersTable tbody');
    if (tbody) {
        tbody.innerHTML = `
            <tr>
                <td colspan="5" class="text-center" style="padding: 30px; color: #ef4444; font-weight: 600;">
                    ⚠️ Error de conexión con el backend. Asegurate de que el servidor esté corriendo.
                </td>
            </tr>`;
    }
}

/**
 * Utilidad: Limita las llamadas consecutivas al servidor al escribir en el buscador
 */
function debounce(func, delay) {
    let timeoutId;
    return (...args) => {
        clearTimeout(timeoutId);
        timeoutId = setTimeout(() => {
            func.apply(null, args);
        }, delay);
    };
}

/**
 * Utilidad: Formatea strings de fecha ISO a la estética de Alkywall (ej: "09 de jun de 2026")
 */
function formatearFecha(fechaString) {
    if (!fechaString) return '-';
    const fecha = new Date(fechaString);
    return fecha.toLocaleDateString('es-ES', {
        day: '2-digit',
        month: 'short',
        year: 'numeric'
    }).replace('.', '');
}

/**
 * Utilidad: Permite cerrar sesión limpiando las credenciales de administración
 */
function cerrarSesion() {
    localStorage.removeItem('admin_token');
    window.location.href = 'login.html';

}
