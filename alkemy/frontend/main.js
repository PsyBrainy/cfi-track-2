// main.js — punto de entrada de la app Alkywall
const BaseUrl = 'http://localhost:8080'; // Url Base, en local, si se sube el proyecto a un servidor cambiar esta URL
document.addEventListener('DOMContentLoaded', () => {
  console.log('Alkywall: app inicializada');

  // Nav responsive con hamburguesa: líneas 7-23
  const navToggle = document.querySelector('.nav-toggle');
  const nav = document.querySelector('.nav');

  if (navToggle && nav) {
    navToggle.addEventListener('click', () => {
      const isOpen = nav.classList.toggle('nav--open');
      navToggle.setAttribute('aria-expanded', isOpen);
    });

    // Cerrar el menú al hacer click en un link
    nav.querySelectorAll('a').forEach(link => {
      link.addEventListener('click', () => {
        nav.classList.remove('nav--open');
        navToggle.setAttribute('aria-expanded', 'false');
      });
    });
  }

  const startButton = document.getElementById('start-button'); // botón "Empezar ahora" del hero
  const registerSection = document.getElementById('registro'); // sección del formulario de registro

  if (startButton && registerSection) {
    startButton.addEventListener('click', () => {
      registerSection.classList.remove('auth-section--hidden'); // saca el display:none, la hace visible
      registerSection.scrollIntoView({ behavior: 'smooth' }); // desplaza la vista hasta la sección
    });
  }

  // Link "Iniciar sesión" del nav: abre la pantalla de login de la misma forma
  const navLoginLink = document.getElementById('nav-login-link');
  const loginSection = document.getElementById('login');

  if (navLoginLink && loginSection) {
    navLoginLink.addEventListener('click', (event) => {
      event.preventDefault(); // evita el salto brusco del href="#login"
      loginSection.classList.remove('auth-section--hidden'); // muestra la sección de login
      loginSection.scrollIntoView({ behavior: 'smooth' }); // scroll suave hasta ella
    });
  }

  // Refs del form de login: se declaran acá arriba (y no solo adentro del
  // bloque "if (loginForm)") porque el flujo de registro exitoso también
  // necesita precargar el email y mostrar el mensaje de éxito en login.
  const loginEmailInput = document.getElementById('login-email');
  const loginPasswordInput = document.getElementById('login-password');
  const loginFeedback = document.getElementById('login-feedback');

  // ============================================================
  // Formulario de REGISTRO: líneas 48-259
  // Campos requeridos por el backend en /api/auth/register:
  // name, lastName, email, password, birthDate, gender, dni,
  // phoneNumber, employment, address, city, province, postalCode
  // ============================================================
  const registerForm = document.getElementById('register-form');

  // --- Provincias y ciudades vía API pública de Georef (datos.gob.ar) ---
  // Documentación: https://datosgobar.github.io/georef-ar-api/
  const GEOREF_BASE = 'https://apis.datos.gob.ar/georef/api';

  if (registerForm) {
    // --- Captura de todos los inputs con getElementById ---
    const nombreInput = document.getElementById('nombre');
    const apellidoInput = document.getElementById('apellido');
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');
    const fechaNacimientoInput = document.getElementById('fechaNacimiento');
    const generoInput = document.getElementById('genero');
    const dniInput = document.getElementById('dni');
    const telefonoInput = document.getElementById('telefono');
    const ocupacionInput = document.getElementById('ocupacion');
    const direccionInput = document.getElementById('direccion');
    const provinciaInput = document.getElementById('provincia');
    const ciudadInput = document.getElementById('ciudad');
    const codigoPostalInput = document.getElementById('codigoPostal');
    const registerFeedback = document.getElementById('register-feedback');

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/; // regex básica: algo@algo.algo
    const MIN_PASSWORD_LENGTH = 8; // longitud mínima requerida

    // Carga las 24 provincias apenas se abre la página y llena el <select>
    async function cargarProvincias() {
      try {
        const response = await fetch(`${GEOREF_BASE}/provincias?campos=nombre&orden=nombre&max=24`);
        const data = await response.json();

        provinciaInput.innerHTML = '<option value="">Seleccioná una provincia</option>';
        data.provincias.forEach((provincia) => {
          const option = document.createElement('option');
          option.value = provincia.nombre;
          option.textContent = provincia.nombre;
          provinciaInput.appendChild(option);
        });

        provinciaInput.disabled = false; // habilita el select una vez cargado
      } catch (error) {
        provinciaInput.innerHTML = '<option value="">No se pudieron cargar las provincias</option>';
        console.error('Error al cargar provincias desde Georef:', error);
      }
    }

    // Carga las ciudades (municipios) de la provincia elegida
    async function cargarCiudades(nombreProvincia) {
      ciudadInput.disabled = true;
      ciudadInput.innerHTML = '<option value="">Cargando ciudades...</option>';

      try {
        const url = `${GEOREF_BASE}/municipios?provincia=${encodeURIComponent(nombreProvincia)}&campos=nombre&orden=nombre&max=300`;
        const response = await fetch(url);
        const data = await response.json();

        ciudadInput.innerHTML = '<option value="">Seleccioná una ciudad</option>';
        data.municipios.forEach((municipio) => {
          const option = document.createElement('option');
          option.value = municipio.nombre;
          option.textContent = municipio.nombre;
          ciudadInput.appendChild(option);
        });

        ciudadInput.disabled = false; // habilita el select una vez cargado
      } catch (error) {
        ciudadInput.innerHTML = '<option value="">No se pudieron cargar las ciudades</option>';
        console.error('Error al cargar ciudades desde Georef:', error);
      }
    }

    cargarProvincias(); // dispara la carga inicial de provincias

    // Cuando el usuario elige una provincia, se cargan sus ciudades
    provinciaInput.addEventListener('change', () => {
      if (!provinciaInput.value) {
        ciudadInput.disabled = true;
        ciudadInput.innerHTML = '<option value="">Elegí primero una provincia</option>';
        return;
      }
      cargarCiudades(provinciaInput.value);
    });

    // --- Mostrar/limpiar error debajo de un input puntual ---
    function mostrarError(input, mensaje) {
      const errorSpan = document.getElementById(`${input.id}-error`);
      errorSpan.textContent = mensaje;
      input.classList.add('input--error');
    }

    function limpiarError(input) {
      const errorSpan = document.getElementById(`${input.id}-error`);
      errorSpan.textContent = '';
      input.classList.remove('input--error');
    }

    // --- Mensaje general de error del servidor ---
    function mostrarFeedbackRegistro(mensaje) {
      registerFeedback.textContent = mensaje;
      registerFeedback.classList.add('form-feedback--error');
    }

    function limpiarFeedbackRegistro() {
      registerFeedback.textContent = '';
      registerFeedback.classList.remove('form-feedback--error');
    }

    // Helper genérico para los campos que solo necesitan "no estar vacío"
    function validarNoVacio(input, mensaje) {
      if (input.value.trim() === '') {
        mostrarError(input, mensaje);
        return false;
      }
      limpiarError(input);
      return true;
    }

    registerForm.addEventListener('submit', (event) => {
      event.preventDefault(); // evita la recarga de página / request innecesario

      limpiarFeedbackRegistro(); // saca cualquier error de servidor de un intento anterior

      let esValido = true; // asumimos válido hasta encontrar un error

      // Nombre y apellido: no vacíos
      if (!validarNoVacio(nombreInput, 'El nombre no puede estar vacío.')) esValido = false;
      if (!validarNoVacio(apellidoInput, 'El apellido no puede estar vacío.')) esValido = false;

      // Email: no vacío y formato válido
      if (emailInput.value.trim() === '') {
        mostrarError(emailInput, 'El email no puede estar vacío.');
        esValido = false;
      } else if (!emailRegex.test(emailInput.value.trim())) {
        mostrarError(emailInput, 'Ingresá un email válido.');
        esValido = false;
      } else {
        limpiarError(emailInput);
      }

      // Contraseña: no vacía y longitud mínima
      if (passwordInput.value === '') {
        mostrarError(passwordInput, 'La contraseña no puede estar vacía.');
        esValido = false;
      } else if (passwordInput.value.length < MIN_PASSWORD_LENGTH) {
        mostrarError(passwordInput, `Debe tener al menos ${MIN_PASSWORD_LENGTH} caracteres.`);
        esValido = false;
      } else {
        limpiarError(passwordInput);
      }

      // Fecha de nacimiento, género, DNI, teléfono, ocupación, dirección: no vacíos
      if (!validarNoVacio(fechaNacimientoInput, 'Ingresá tu fecha de nacimiento.')) esValido = false;
      if (!validarNoVacio(generoInput, 'Seleccioná una opción.')) esValido = false;
      if (!validarNoVacio(dniInput, 'El DNI no puede estar vacío.')) esValido = false;
      if (!validarNoVacio(telefonoInput, 'El teléfono no puede estar vacío.')) esValido = false;
      if (!validarNoVacio(ocupacionInput, 'Seleccioná una opción.')) esValido = false;
      if (!validarNoVacio(direccionInput, 'La dirección no puede estar vacía.')) esValido = false;

      // Provincia y ciudad: no vacías
      if (!validarNoVacio(provinciaInput, 'Seleccioná una provincia.')) esValido = false;
      if (!validarNoVacio(ciudadInput, 'Seleccioná una ciudad.')) esValido = false;

      // Código postal: no vacío
      if (!validarNoVacio(codigoPostalInput, 'El código postal no puede estar vacío.')) esValido = false;

      // Si la validación del navegador falla, no se hace ningún request
      if (!esValido) {
        return;
      }

      // --- Petición POST al backend con los 13 campos requeridos ---
      fetch(`${BaseUrl}/api/auth/register`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          name: nombreInput.value.trim(),
          lastName: apellidoInput.value.trim(),
          email: emailInput.value.trim(),
          password: passwordInput.value,
          birthDate: fechaNacimientoInput.value, // formato YYYY-MM-DD que entrega <input type="date">
          gender: generoInput.value,
          dni: dniInput.value.trim(),
          phoneNumber: telefonoInput.value.trim(),
          employment: ocupacionInput.value,
          address: direccionInput.value.trim(),
          city: ciudadInput.value,
          province: provinciaInput.value,
          postalCode: codigoPostalInput.value.trim()
        })
      })
        .then((response) => {
          if (response.ok) {
            console.log('Registro exitoso');

            const emailRegistrado = emailInput.value.trim();
            registerForm.reset(); // limpia todos los campos para la próxima vez

            // Ocultamos el registro y mostramos el login para que inicie sesión
            registerSection.classList.add('auth-section--hidden');
            loginSection.classList.remove('auth-section--hidden');
            loginSection.scrollIntoView({ behavior: 'smooth' });

            // Precargamos el email para que no lo tenga que tipear de nuevo
            if (loginEmailInput) {
              loginEmailInput.value = emailRegistrado;
            }

            // Mensaje de éxito (verde, distinto del feedback de error)
            if (loginFeedback) {
              loginFeedback.textContent = 'Cuenta creada con éxito. Iniciá sesión para continuar.';
              loginFeedback.classList.remove('form-feedback--error');
              loginFeedback.classList.add('form-feedback--success');
            }

            return;
          }
 

          // Cualquier error del servidor (400, 409 email duplicado, etc.)
          return response.json()
            .then((data) => {
              mostrarFeedbackRegistro(data.message || 'No se pudo completar el registro.');
            })
            .catch(() => {
              mostrarFeedbackRegistro('No se pudo completar el registro.');
            });
        })
        .catch(() => {
          mostrarFeedbackRegistro('No se pudo conectar con el servidor.');
        });
    });
  }

  // ============================================================
  // Formulario de LOGIN: líneas 268-376
  // ============================================================

  // --- Captura del form y sus inputs con getElementById ---
  const loginForm = document.getElementById('login-form'); // captura el <form> de login

  if (loginForm) {
             const saldoElement = document.getElementById('saldo-disponible');
          //formatear moneda
          function formatearMoneda(valor){
          return new Intl.NumberFormat('es-AR', {style:
          'currency',currency: 'ARS' }).format(valor)}

    // loginEmailInput, loginPasswordInput y loginFeedback ya están declarados
    // más arriba en este mismo scope (ver comentario junto a nav-login-link)

    const loginEmailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/; // misma regex básica que en registro

    // Funciones para mostrar/limpiar error debajo de un input puntual
    function mostrarErrorLogin(input, mensaje) {
      const errorSpan = document.getElementById(`${input.id}-error`); // busca el <span> asociado
      errorSpan.textContent = mensaje; // escribe el mensaje debajo del input
      input.classList.add('input--error'); // pinta el borde en rojo
    }

    function limpiarErrorLogin(input) {
      const errorSpan = document.getElementById(`${input.id}-error`); // busca el <span> asociado
      errorSpan.textContent = ''; // borra el mensaje
      input.classList.remove('input--error'); // saca el borde rojo
    }

    // Muestra el mensaje general de error del servidor (401/403)
    function mostrarFeedback(mensaje) {
      loginFeedback.textContent = mensaje; // escribe el texto del error
      loginFeedback.classList.add('form-feedback--error'); // le da el fondo/borde rojo
    }

    // Limpia el mensaje general de error del servidor
    function limpiarFeedback() {
      loginFeedback.textContent = ''; // borra el texto
      loginFeedback.classList.remove('form-feedback--error'); // saca el estilo de error
      loginFeedback.classList.remove('form-feedback--success'); // saca el estilo de éxito
    }

    // --- Evento submit + preventDefault ---
    loginForm.addEventListener('submit', (event) => {
      event.preventDefault(); // evita la recarga de página / request innecesario

      limpiarFeedback(); // saca cualquier error de intento anterior antes de validar de nuevo

      let esValido = true; // asumimos válido hasta encontrar un error

      // --- Validación: email no vacío y con formato correcto ---
      if (loginEmailInput.value.trim() === '') {
        mostrarErrorLogin(loginEmailInput, 'El email no puede estar vacío.');
        esValido = false;
      } else if (!loginEmailRegex.test(loginEmailInput.value.trim())) {
        mostrarErrorLogin(loginEmailInput, 'Ingresá un email válido.');
        esValido = false;
      } else {
        limpiarErrorLogin(loginEmailInput);
      }

      // --- Validación: contraseña no vacía ---
      if (loginPasswordInput.value === '') {
        mostrarErrorLogin(loginPasswordInput, 'La contraseña no puede estar vacía.');
        esValido = false;
      } else {
        limpiarErrorLogin(loginPasswordInput);
      }

      // Si la validación del navegador falla, NO se hace ningún request al servidor
      if (!esValido) {
        return;
      }

      // --- Petición POST al backend con fetch ---
      fetch(`${BaseUrl}/api/auth/login`, {
        method: 'POST', // verbo POST, como pide la consigna
        headers: {
          'Content-Type': 'application/json' // le avisamos al backend que mandamos JSON
        },
        body: JSON.stringify({
          email: loginEmailInput.value.trim(), // credencial: email
          password: loginPasswordInput.value // credencial: contraseña
        })
      })
        .then((response) => {
          // Caso éxito: el backend respondió 200
          if (response.ok) {
            return response.json().then((data) => {
              // Extraemos el token JWT de la respuesta y lo guardamos en el navegador
              localStorage.setItem('token', data.token);
              console.log('Login exitoso, token guardado en localStorage');

              // ocultar login y billetera, mostrar dashboard*
              loginSection.classList.add('auth-section--hidden');
              document.getElementById('registro').classList.add('auth-section--hidden');
              document.getElementById('billetera').classList.add('auth-section--hidden');
              document.getElementById('start-button').style.display = 'none';
              document.getElementById('dashboard').classList.remove('auth-section--hidden');

              //Fetch del saldo con el token
              fetch(`${BaseUrl}/api/account/balance`, {
                headers: {'Authorization': `Bearer ${data.token}`}
              })
              .then(res => res.json())
              .then(datos => {
                saldoElement.textContent = formatearMoneda(datos.balance);
              })
              .catch(()=> {
                saldoElement.textContent = 'Error al cargar saldo';
              });
            });
          }

          // Caso error: credenciales inválidas (401 o 403)
          if (response.status === 401 || response.status === 403) {
            mostrarFeedback('Email o contraseña incorrectos.');
            return;
          }

          // Cualquier otro código de error inesperado del servidor
          mostrarFeedback('Ocurrió un error al iniciar sesión. Intentá de nuevo.');
        })
        .catch(() => {
          // Error de red (servidor caído, sin conexión, etc.)
          mostrarFeedback('No se pudo conectar con el servidor.');
        });
    });
  }
});