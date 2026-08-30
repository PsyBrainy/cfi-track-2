// main.js — punto de entrada de la app Alkywall

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

  // Validación del formulario de registro: líneas 47-110
  const registerForm = document.getElementById('register-form');

  if (registerForm) {
    const nombreInput = document.getElementById('nombre'); // input de nombre
    const emailInput = document.getElementById('email'); // input de email
    const passwordInput = document.getElementById('password'); // input de contraseña

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/; // regex básica: algo@algo.algo
    const MIN_PASSWORD_LENGTH = 8; // longitud mínima requerida

    function mostrarError(input, mensaje) {
      const errorSpan = document.getElementById(`${input.id}-error`); // busca el <span> asociado al input
      errorSpan.textContent = mensaje; // muestra el mensaje de error debajo del input
      input.classList.add('input--error'); // pinta el borde del input en rojo
    }

    function limpiarError(input) {
      const errorSpan = document.getElementById(`${input.id}-error`); // busca el <span> asociado al input
      errorSpan.textContent = ''; // borra el mensaje de error
      input.classList.remove('input--error'); // saca el borde rojo
    }

    registerForm.addEventListener('submit', (event) => {
      event.preventDefault(); // evita la recarga de página / request innecesario

      let esValido = true; // asumimos válido hasta encontrar un error

      // Nombre: no vacío
      if (nombreInput.value.trim() === '') {
        mostrarError(nombreInput, 'El nombre no puede estar vacío.');
        esValido = false;
      } else {
        limpiarError(nombreInput);
      }

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

      if (esValido) {
        console.log('Formulario válido, listo para enviar al servidor');
        // Acá luego se agrega el fetch/POST al backend (alkywallet)
      }
    });
  }

  // ============================================================
  // Formulario de LOGIN: líneas 112-220
  // ============================================================

  // --- Captura del form y sus inputs con getElementById ---
  const loginForm = document.getElementById('login-form'); // captura el <form> de login

  if (loginForm) {
    const loginEmailInput = document.getElementById('login-email'); // input de email
    const loginPasswordInput = document.getElementById('login-password'); // input de contraseña
    const loginFeedback = document.getElementById('login-feedback'); // div de error general (401/403)

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
      fetch('/api/auth/login', {
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
              // Acá luego se puede redirigir al usuario a su dashboard/billetera
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