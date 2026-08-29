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

  const startButton = document.querySelector('.btn--primary'); // botón "Empezar ahora" del hero
  const authSection = document.getElementById('registro'); // sección del formulario de registro

  if (startButton && authSection) {
    startButton.addEventListener('click', () => {
      authSection.classList.remove('auth-section--hidden'); // saca el display:none, la hace visible
      authSection.scrollIntoView({ behavior: 'smooth' }); // desplaza la vista hasta la sección
    });
  }

  // Validación del formulario de registro: líneas 34-96
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
});