// registro.js — lógica de la página de REGISTRO.
// Campos requeridos por el backend en /api/auth/register:
// name, lastName, email, password, birthDate, gender, dni,
// phoneNumber, employment, address, city, province, postalCode

import { BaseUrl } from './config.js';
import { cargarProvincias, cargarCiudades } from './georef.js';

const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const MIN_PASSWORD_LENGTH = 8;

export function initRegistro() {
  const registerForm = document.getElementById('register-form');
  if (!registerForm) return;

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
  const aceptarTerminosInput = document.getElementById('aceptar-terminos');
  const aceptarCuentaInput = document.getElementById('aceptar-cuenta');

  // Modal de términos y condiciones
  const terminosModal = document.getElementById('terminos-modal');
  const terminosLink = document.getElementById('terminos-link');

  function abrirTerminos() {
    if (terminosModal) terminosModal.classList.add('modal--open');
  }

  function cerrarTerminos() {
    if (terminosModal) terminosModal.classList.remove('modal--open');
  }

  if (terminosLink) {
    terminosLink.addEventListener('click', (event) => {
      event.preventDefault();
      abrirTerminos();
    });
  }

  if (terminosModal) {
    terminosModal.querySelectorAll('[data-modal-close]').forEach((el) => {
      el.addEventListener('click', cerrarTerminos);
    });
    document.addEventListener('keydown', (event) => {
      if (event.key === 'Escape') cerrarTerminos();
    });
  }

  function mostrarError(input, mensaje) {
    const errorSpan = document.getElementById(`${input.id}-error`);
    if (errorSpan) errorSpan.textContent = mensaje;
    if (input.type === 'checkbox') {
      input.parentElement.classList.add('checkbox--error');
    } else {
      input.classList.add('input--error');
    }
  }

  function limpiarError(input) {
    const errorSpan = document.getElementById(`${input.id}-error`);
    if (errorSpan) errorSpan.textContent = '';
    if (input.type === 'checkbox') {
      input.parentElement.classList.remove('checkbox--error');
    } else {
      input.classList.remove('input--error');
    }
  }

  function validarNoVacio(input, mensaje) {
    if (input.value.trim() === '') {
      mostrarError(input, mensaje);
      return false;
    }
    limpiarError(input);
    return true;
  }

  function mostrarFeedbackRegistro(mensaje) {
    registerFeedback.textContent = mensaje;
    registerFeedback.classList.add('form-feedback--error');
    registerFeedback.classList.remove('form-feedback--success');
  }

  function limpiarFeedbackRegistro() {
    registerFeedback.textContent = '';
    registerFeedback.classList.remove('form-feedback--error');
    registerFeedback.classList.remove('form-feedback--success');
  }

  // Carga inicial de provincias y bindeo de ciudades
  cargarProvincias(provinciaInput, ciudadInput);

  provinciaInput.addEventListener('change', () => {
    if (!provinciaInput.value) {
      ciudadInput.disabled = true;
      ciudadInput.innerHTML = '<option value="">Elegí primero una provincia</option>';
      return;
    }
    cargarCiudades(provinciaInput.value, ciudadInput);
  });

  // Limpia el error del checkbox apenas el usuario lo marca
  aceptarTerminosInput.addEventListener('change', () => {
    if (aceptarTerminosInput.checked) limpiarError(aceptarTerminosInput);
  });

  aceptarCuentaInput.addEventListener('change', () => {
    if (aceptarCuentaInput.checked) limpiarError(aceptarCuentaInput);
  });

  registerForm.addEventListener('submit', (event) => {
    event.preventDefault();
    limpiarFeedbackRegistro();

    let esValido = true;

    if (!validarNoVacio(nombreInput, 'El nombre no puede estar vacío.')) esValido = false;
    if (!validarNoVacio(apellidoInput, 'El apellido no puede estar vacío.')) esValido = false;

    if (emailInput.value.trim() === '') {
      mostrarError(emailInput, 'El email no puede estar vacío.');
      esValido = false;
    } else if (!emailRegex.test(emailInput.value.trim())) {
      mostrarError(emailInput, 'Ingresá un email válido.');
      esValido = false;
    } else {
      limpiarError(emailInput);
    }

    if (passwordInput.value === '') {
      mostrarError(passwordInput, 'La contraseña no puede estar vacía.');
      esValido = false;
    } else if (passwordInput.value.length < MIN_PASSWORD_LENGTH) {
      mostrarError(passwordInput, `Debe tener al menos ${MIN_PASSWORD_LENGTH} caracteres.`);
      esValido = false;
    } else {
      limpiarError(passwordInput);
    }

    if (!validarNoVacio(fechaNacimientoInput, 'Ingresá tu fecha de nacimiento.')) esValido = false;
    if (!validarNoVacio(generoInput, 'Seleccioná una opción.')) esValido = false;
    if (!validarNoVacio(dniInput, 'El DNI no puede estar vacío.')) esValido = false;
    if (!validarNoVacio(telefonoInput, 'El teléfono no puede estar vacío.')) esValido = false;
    if (!validarNoVacio(ocupacionInput, 'Seleccioná una opción.')) esValido = false;
    if (!validarNoVacio(direccionInput, 'La dirección no puede estar vacía.')) esValido = false;
    if (!validarNoVacio(provinciaInput, 'Seleccioná una provincia.')) esValido = false;
    if (!validarNoVacio(ciudadInput, 'Seleccioná una ciudad.')) esValido = false;
    if (!validarNoVacio(codigoPostalInput, 'El código postal no puede estar vacío.')) esValido = false;

    // Checkbox de términos y condiciones (obligatorio)
    if (aceptarTerminosInput && !aceptarTerminosInput.checked) {
      mostrarError(aceptarTerminosInput, 'Debés aceptar los términos y condiciones.');
      esValido = false;
    } else if (aceptarTerminosInput) {
      limpiarError(aceptarTerminosInput);
    }

    // Checkbox de creación de cuenta bancaria Alkywall (obligatorio)
    if (aceptarCuentaInput && !aceptarCuentaInput.checked) {
      mostrarError(aceptarCuentaInput, 'Debés aceptar la creación de la cuenta bancaria Alkywall.');
      esValido = false;
    } else if (aceptarCuentaInput) {
      limpiarError(aceptarCuentaInput);
    }

    if (!esValido) return;

    fetch(`${BaseUrl}/api/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        name: nombreInput.value.trim(),
        lastName: apellidoInput.value.trim(),
        email: emailInput.value.trim(),
        password: passwordInput.value,
        birthDate: fechaNacimientoInput.value,
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
          registerForm.reset();
          // Redirige a login con el email precargado para que inicie sesión
          window.location.href = `login.html?email=${encodeURIComponent(emailInput.value.trim())}`;
          return;
        }
        return response
          .json()
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
