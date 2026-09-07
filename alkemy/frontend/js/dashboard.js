// js/dashboard.js - VERSIÓN CORREGIDA DIRECTA A BALANCE
import { BaseUrl } from './config.js';

const URL_BALANCE_REAL = `${BaseUrl}/api/account/balance`; // Endpoint exitoso de tu red
const URL_TRANSACTIONS = `${BaseUrl}/api/account/transactions`; 
const URL_CATEGORIES = `${BaseUrl}/api/account/expenses-summary`; 

export async function initDashboard() {
    const token = localStorage.getItem('token');

    if (!token) {
        window.location.href = './login.html';
        return;
    }

    try {
        // 1. Extraemos dinámicamente el nombre desde el JWT para el saludo
        try {
            const base64Url = token.split('.');
            const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
            const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
                return '%' + ('0' + c.charCodeAt(0).toString(16)).slice(-2);
            }).join(''));
            
            const tokenData = JSON.parse(jsonPayload);
            const emailUser = tokenData.sub || "Facundo";
            const nombreMapeado = emailUser.split('@');
            const nombreCapitalizado = nombreMapeado.charAt(0).toUpperCase() + nombreMapeado.slice(1);
            
            document.getElementById('user-greeting').textContent = `¡Hola, ${nombreCapitalizado}!`;
        } catch (e) {
            document.getElementById('user-greeting').textContent = "¡Hola, Facundo!";
        }

        // 2. Consumimos el saldo real desde tu endpoint /api/account/balance
        const balanceRes = await fetch(URL_BALANCE_REAL, { 
            headers: { 'Authorization': `Bearer ${token}` } 
        });

        if (balanceRes.status === 401 || balanceRes.status === 403) {
            localStorage.removeItem('token');
            window.location.href = './login.html?motivo=expirado';
            return;
        }

        if (balanceRes.ok) {
            const data = await balanceRes.json();
            
            // Leemos las claves exactas en castellano que te muestra la consola (saldoDisponible)
            const saldoReal = data.saldoDisponible || 0;
            const monedaReal = data.moneda || "ARS";

            // Inyectamos el dinero en el saldo disponible e ingresos del mes de respaldo
            document.getElementById('total-balance').textContent = formatCurrency(saldoReal);
            document.getElementById('monthly-income').textContent = formatCurrency(saldoReal);
            document.getElementById('monthly-expenses').textContent = formatCurrency(0);
            
            document.getElementById('income-percentage').textContent = monedaReal;
            document.getElementById('expense-percentage').textContent = monedaReal;
        }

        // 3. Renderizamos los paneles analíticos inferiores
        await cargarActividadReciente(token);
        await cargarDistribucionGastos(token);

    } catch (error) {
        console.error('Error al sincronizar las tablas con el dashboard:', error);
    }
}

async function cargarActividadReciente(token) {
    const tbody = document.getElementById('recent-transactions-tbody');
    if (!tbody) return;

    try {
        const response = await fetch(URL_TRANSACTIONS, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!response.ok) throw new Error("No se pudo obtener el historial");
        
        const movimientos = await response.json(); 

        if (!movimientos || movimientos.length === 0) {
            tbody.innerHTML = `<tr><td colspan="4" class="text-center table-loading">No registrás transferencias este mes.</td></tr>`;
            return;
        }

        tbody.innerHTML = '';
        movimientos.slice(0, 5).forEach(mov => {
            const tipo = String(mov.type || mov.type_transaction || '').toUpperCase();
            const esIngreso = tipo === 'INGRESO' || tipo === 'DEPOSITO' || tipo === '';
            const signo = esIngreso ? '+' : '-';
            const claseMonto = esIngreso ? 'income' : 'expense';
            
            const fechaReal = mov.date_transaction || mov.dateTransaction || mov.datetransaction;
            const concepto = mov.movement_type || mov.movementType || mov.movementtype || (esIngreso ? 'Depósito Realizado' : 'Transferencia Enviada');
            const montoReal = mov.amount || 0;

            tbody.innerHTML += `
                <tr>
                    <td><strong>${concepto}</strong></td>
                    <td><span class="status-tag completed">COMPLETED</span></td>
                    <td>${formatDate(fechaReal)}</td>
                    <td class="text-right tx-amount ${claseMonto}">${signo} ${formatCurrency(montoReal)}</td>
                </tr>
            `;
        });

    } catch (error) {
        console.error('Error cargando actividad reciente:', error);
        tbody.innerHTML = `<tr><td colspan="4" class="text-center table-loading" style="color: #ef4444;">Error al conectar con el historial.</td></tr>`;
    }
}

async function cargarDistribucionGastos(token) {
    const container = document.getElementById('categories-distribution-container');
    if (!container) return;

    try {
        const response = await fetch(URL_CATEGORIES, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!response.ok) throw new Error("No se pudo obtener la distribución");

        const gastosAgrupados = await response.json(); 

        if (!gastosAgrupados || gastosAgrupados.length === 0) {
            container.innerHTML = `<div class="table-loading">No registrás egresos en la tabla de transferencias.</div>`;
            return;
        }

        const totalGastos = gastosAgrupados.reduce((sum, item) => sum + (item.amount || item.total || 0), 0);

        container.innerHTML = '';
        gastosAgrupados.forEach(item => {
            const monto = item.amount || item.total || 0;
            const concepto = item.movement_type || item.movementType || item.movementtype || 'Transferencias';
            const porcentajeBarra = totalGastos > 0 ? Math.round((monto / totalGastos) * 100) : 0;

            container.innerHTML += `
                <div class="category-item">
                    <div class="category-info">
                        <span>${concepto}</span>
                        <span>${formatCurrency(monto)}</span>
                    </div>
                    <div class="category-progress-bar">
                        <div class="category-progress-fill" style="width: ${porcentajeBarra}%"></div>
                    </div>
                </div>
            `;
        });

    } catch (error) {
        console.error('Error cargando distribución de gastos:', error);
        container.innerHTML = `<div class="table-loading" style="color: #ef4444;">Error al procesar el resumen de la tabla.</div>`;
    }
}

function formatCurrency(valor) {
    return new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS' }).format(valor);
}

function formatDate(fechaStr) {
    if (!fechaStr) return '-';
    const opciones = { year: 'numeric', month: 'short', day: 'numeric' };
    return new Date(fechaStr).toLocaleDateString('es-AR', opciones);
}
