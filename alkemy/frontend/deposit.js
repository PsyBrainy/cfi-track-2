const API_BASE_URL = 'http://localhost:8080';
const BALANCE_URL = `${API_BASE_URL}/api/account/balance`;
const DEPOSIT_URL = `${API_BASE_URL}/api/account/deposit`;

document.addEventListener('DOMContentLoaded', loadBalance);

async function loadBalance() {
    const token = localStorage.getItem('token');
    const balanceElement = document.getElementById('saldo');

    if (!token) {
        redirectToLogin();
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
            redirectToLogin();
            return;
        }

        const data = await readJsonResponse(response);
        if (!response.ok) {
            throw new Error(data.message || 'No se pudo consultar el saldo.');
        }

        balanceElement.textContent = Number(data.saldoDisponible ?? data.availableBalance ?? 0).toFixed(2);
    } catch (error) {
        balanceElement.textContent = '0.00';
        showMessage('No se pudo cargar el saldo', error.message || 'Hubo un problema al consultar el saldo.', 'error');
    }
}

async function depositar() {
    const token = localStorage.getItem('token');
    const amountInput = document.getElementById('monto');
    const amount = Number(amountInput.value);

    if (!token) {
        redirectToLogin();
        return;
    }

    if (!Number.isFinite(amount) || amount <= 0) {
        showMessage('Monto inválido', 'Por favor, ingrese un monto válido mayor a 0.', 'warning');
        return;
    }

    try {
        const response = await fetch(DEPOSIT_URL, {
            method: 'POST',
            headers: {
                Authorization: `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ monto: amount })
        });

        const data = await readJsonResponse(response);

        if (response.status === 401 || response.status === 403) {
            redirectToLogin();
            return;
        }

        if (!response.ok) {
            throw new Error(data.message || 'No se pudo realizar el depósito.');
        }

        amountInput.value = '';
        await loadBalance();
        showMessage('Depósito realizado', 'El saldo fue actualizado correctamente.', 'success');
    } catch (error) {
        showMessage('No se pudo realizar el depósito', error.message || 'Hubo un problema al realizar el depósito.', 'error');
    }
}

function redirectToLogin() {
    localStorage.removeItem('token');
    Swal.fire({
        icon: 'warning',
        title: 'Sesión expirada',
        text: 'Iniciá sesión nuevamente para continuar.',
        confirmButtonText: 'Aceptar',
        confirmButtonColor: '#2563eb'
    }).then(() => {
        window.location.href = 'index.html';
    });
}

function showMessage(title, text, icon) {
    Swal.fire({
        icon,
        title,
        text,
        confirmButtonText: 'Aceptar',
        confirmButtonColor: '#2563eb'
    });
}

async function readJsonResponse(response) {
    const contentType = response.headers.get('content-type') || '';

    if (!contentType.includes('application/json')) {
        return {};
    }

    return response.json();
}
