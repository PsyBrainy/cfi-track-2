
const API_URL = 'http://localhost:8080/api/account/transactions';


const contenedor = document.getElementById('contenedor-movimientos');
contenedor.innerHTML = `<tr><td colspan="5" class="estado-loading">Buscando transacciones...</td></tr>`;

async function cargarHistorial() {
    const token = localStorage.getItem('token');

    if (!token) {
        window.location.href = 'index.html';
        return;
    }

    try {

        // Consumir el endpoint de historial a través de fetch o Axios, enviando el token JWT.
        const response = await fetch(API_URL, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (response.status === 401 || response.status === 403) {
            localStorage.removeItem('token');
            window.location.href = 'index.html';
            return;
        }

        const data = await response.json();
        if (!response.ok) {
            throw new Error(data.message || 'No se pudieron obtener datos del servidor');
        }

        const movimientos = data;
        contenedor.innerHTML = '';

        if (movimientos.length === 0) {
            contenedor.innerHTML = `<tr><td colspan="5" style="text-align:center; padding:1rem;">No hay movimientos registrados.</td></tr>`;
            return;
        }

        movimientos.forEach((movimiento) => {
            const fila = document.createElement('tr');
            const tipo = movimiento.type || '-';
            const esIngreso = tipo === 'INGRESO';
            const signo = esIngreso ? '+' : '-';
            fila.innerHTML = `
                <td>${formatearFecha(movimiento.dateTransaction)}</td>
                <td class="movimiento-nombre">${movimiento.movementType || '-'}</td>
                <td><span class="tipo-badge ${esIngreso ? 'tipo-ingreso' : 'tipo-egreso'}">${tipo}</span></td>
                <td class="importe ${esIngreso ? 'importe-ingreso' : 'importe-egreso'}">${signo}${formatearMoneda(movimiento.amount)}</td>
                <td>${formatearMoneda(movimiento.balance)}</td>
            `;
            contenedor.appendChild(fila);
        });


    } catch (error) {
        console.error('Error:', error);
        contenedor.innerHTML = `<tr><td colspan="4" class="estado-error">Error, no se puede contactar con el servidor...</td></tr>`;
    }
}

function formatearFecha(fecha) {
    if (!fecha) {
        return '-';
    }

    const fechaNormalizada = fecha.includes('T') ? fecha : fecha.replace(' ', 'T');
    const fechaObjeto = new Date(fechaNormalizada);

    return Number.isNaN(fechaObjeto.getTime())
        ? fecha
        : fechaObjeto.toLocaleString('es-AR');
}

function formatearMoneda(valor) {
    const monto = Number(valor);
    return Number.isFinite(monto)
        ? new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS' }).format(monto)
        : '$0,00';
}


document.addEventListener('DOMContentLoaded', cargarHistorial);