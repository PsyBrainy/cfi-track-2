let saldoActual = 0

function depositar() {
    const inputMonto = document.getElementById('monto');
    const montoADepositar = parseFloat(inputMonto.value);
    const saldoElemento = document.getElementById('saldo');




    if (isNaN(montoADepositar) || montoADepositar <= 0) {
        alert("Por favor, ingrese un monto válido mayor a 0.");
        return;
    }


    saldoActual += montoADepositar;
    saldoElemento.textContent = saldoActual.toFixed(2);



    inputMonto.value = '';
}