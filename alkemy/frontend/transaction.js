
const API_URL = ''; //aca hay que poner el endpoint de las transacciones
const token = localStorage.getItem('jwt_token');


const contenedor = document.getElementById('contenedor-movimientos');
contenedor.innerHTML = `<tr><td colspan="4" class="estado-loading">Buscando transacciones...</td></tr>`;

async function cargarHistorial() {
    try {

        // Consumir el endpoint de historial a través de fetch o Axios, enviando el token JWT.
        const response = await fetch(API_URL, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error('No se pudieron obtener datos del servidor');
        }

        const movimientos = await response.json();


        contenedor.innerHTML = '';

        if (movimientos.length === 0) {
            contenedor.innerHTML = `<tr><td colspan="4" style="text-align:center; padding:1rem;">No hay movimientos registrados.</td></tr>`;
            return;
        }

        //Iterar sobre el array de datos devuelto (usando forEach o map en JavaScript) y
        // manipular el DOM utilizando document.createElement() e
        // inyectando las filas generadas en un contenedor previamente capturado con getElementById()


    } catch (error) {
        console.error('Error:', error);
        contenedor.innerHTML = `<tr><td colspan="4" class="estado-error">Error, no se puede contactar con el servidor...</td></tr>`;
    }
}


document.addEventListener('DOMContentLoaded', cargarHistorial);