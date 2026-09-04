const API_BASE_URL = 'http://localhost:8080';
const BALANCE_URL = `${API_BASE_URL}/api/account/balance`;
const DEPOSIT_URL = `${API_BASE_URL}/api/account/deposit`;

document.addEventListener('DOMContentLoaded', cargarSaldo);

async function cargarSaldo() {
    const token = localStorage.getItem('token');
    const saldoElemento = document.getElementById('saldo');

    if (!token) {
        window.location.href = 'index.html';
        return;
    }

    try {
        const response = await fetch(BALANCE_URL, {
            method: 'GET',
            headers: {
                Authorization: `Bearer ${token}`
            }
        });

        if (response.status === 401 || response.status === 403) {
            cerrarSesion();
            return;
        }

        const data = await response.json();
        if (!response.ok) {
            throw new Error(data.message || 'No se pudo consultar el saldo.');
        }

        saldoElemento.textContent = Number(data.saldoDisponible ?? data.availableBalance ?? 0).toFixed(2);
    } catch (error) {
        saldoElemento.textContent = '0.00';
        alert(error.message || 'Hubo un problema al consultar el saldo.');
    }
}

async function depositar() {
    const inputMonto = document.getElementById('monto');
    const montoADepositar = parseFloat(inputMonto.value);
    const saldoElemento = document.getElementById('saldo');

    const token = localStorage.getItem('token');

    if (!token) {
        cerrarSesion();
        return;
    }

    if (!Number.isFinite(montoADepositar) || montoADepositar <= 0) {
        alert('Por favor, ingrese un monto válido mayor a 0.');
        return;
    }

    try {
        const response = await fetch(DEPOSIT_URL, {
            method: 'POST',
            headers: {
                Authorization: `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ monto: montoADepositar })
        });

        const data = response.status === 204 ? {} : await response.json();

        if (response.status === 401 || response.status === 403) {
            cerrarSesion();
            return;
        }

        if (!response.ok) {
            throw new Error(data.message || 'No se pudo realizar el depósito.');
        }

        inputMonto.value = '';
        await cargarSaldo();
        alert('Depósito realizado correctamente.');
    } catch (error) {
        alert(error.message || 'Hubo un problema al realizar el depósito.');
    }
}

function cerrarSesion() {
    localStorage.removeItem('token');
    window.location.href = 'index.html';
}
