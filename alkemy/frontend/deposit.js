let saldoActual = 0;

function depositar() {
    const inputMonto = document.getElementById('monto');
    const saldoElemento = document.getElementById('saldo');

    if (!inputMonto || !saldoElemento) {
        return;
    }

    const montoADepositar = parseFloat(inputMonto.value);

    if (isNaN(montoADepositar) || montoADepositar <= 0) {
        alert('Por favor, ingrese un monto válido mayor a 0.');
        return;
    }

    saldoActual += montoADepositar;
    saldoElemento.textContent = saldoActual.toFixed(2);

    inputMonto.value = '';
}
