//Consumir la API REST del backend desde el frontend #28

// Crear una función asíncrona en JavaScript (async/await)
// que se ejecute al cargar la página del dashboard (usando el evento DOMContentLoaded).


document.addEventListener('DOMContentLoaded', async () => {


    // Recuperar el token JWT que guardamos en el localStorage durante el inicio de sesión.
    const token = localStorage.getItem('token');

    // Si no existe el token, redirije a la pagina principal
    if (!token) {
        window.location.href = 'index.html';
        return;
    }

    try {
        // Configurar los headers de la petición para incluir el token: Authorization: 'Bearer ' + token.
        const response = await fetch(URL_BALANCE, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        // Capturar la respuesta. Si el token expiró (código 401/403),
        // redirigir al usuario de vuelta a la pantalla de login
        if (response.status === 401 || response.status === 403) {
            console.warn('Sesión expirada');
            localStorage.removeItem('token'); // borra el recibido token y te manda a la pagina principal
            window.location.href = 'index.html';
            return;
        }

        // Si la respuesta no es exitosa por otra razón (ej. error 500)
        if (!response.ok) {
            throw new Error(`Error: ${response.status}`);
        }


        const data = await response.json();
        console.log('balance cargado con éxito:', data);


    } catch (error) {
        console.error('Hubo un problema al conectar con el servidor:', error);
    }
});