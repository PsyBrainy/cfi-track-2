const API_BASE_URL = 'http://localhost:8080';
const BALANCE_URL = `${API_BASE_URL}/api/account/balance`;
const TRANSFER_URL = `${API_BASE_URL}/api/account/transfer`;

const transferForm = document.getElementById('transfer-form');
const balanceElement = document.getElementById('saldo');

transferForm.addEventListener('submit', async (event) => {
    event.preventDefault();
    await submitTransfer();
});

document.addEventListener('DOMContentLoaded', loadBalance);

async function loadBalance() {
    const token = localStorage.getItem('token');

    if (!token) {
        redirectToLogin();
        return;
    }

    try {
        const response = await fetch(BALANCE_URL, {
            headers: { Authorization: `Bearer ${token}` }
        });
        const data = await readJsonResponse(response);

        if (response.status === 401 || response.status === 403) {
            redirectToLogin();
            return;
        }
        if (!response.ok) {
            throw new Error(data.message || 'No se pudo consultar el saldo.');
        }

        const balance = Number(data.saldoDisponible ?? data.availableBalance ?? 0);
        balanceElement.textContent = Number.isFinite(balance) ? formatCurrency(balance) : formatCurrency(0);
    } catch (error) {
        balanceElement.textContent = formatCurrency(0);
        showMessage('No se pudo cargar el saldo', error.message, 'error');
    }
}

async function submitTransfer() {
    const token = localStorage.getItem('token');
    const destinationEmail = document.getElementById('emailDestino').value.trim();
    const amount = Number(document.getElementById('monto').value);
    const submitButton = transferForm.querySelector('button[type="submit"]');

    if (!token) {
        redirectToLogin();
        return;
    }
    if (!destinationEmail || !Number.isFinite(amount) || amount <= 0) {
        showMessage('Datos incompletos', 'Ingresá un correo válido y un monto mayor a cero.', 'warning');
        return;
    }

    submitButton.disabled = true;
    submitButton.textContent = 'Procesando...';

    try {
        const response = await fetch(TRANSFER_URL, {
            method: 'POST',
            headers: {
                Authorization: `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ emailDestino: destinationEmail, monto: amount })
        });
        const data = await readJsonResponse(response);

        if (response.status === 401 || response.status === 403) {
            redirectToLogin();
            return;
        }
        if (!response.ok) {
            throw new Error(data.message || 'No se pudo realizar la transferencia.');
        }

        transferForm.reset();
        await loadBalance();
        showMessage('Transferencia realizada', 'Los fondos fueron enviados correctamente.', 'success');
    } catch (error) {
        showMessage('No se pudo realizar la transferencia', error.message, 'error');
    } finally {
        submitButton.disabled = false;
        submitButton.textContent = 'Confirmar transferencia';
    }
}

async function readJsonResponse(response) {
    const contentType = response.headers.get('content-type') || '';
    return contentType.includes('application/json') ? response.json() : {};
}

function formatCurrency(value) {
    return new Intl.NumberFormat('es-AR', {
        style: 'currency',
        currency: 'ARS'
    }).format(value);
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
