// georef.js — carga de provincias y ciudades de Argentina vía API pública
// de Georef (datos.gob.ar). Documentación:
// https://datosgobar.github.io/georef-ar-api/

import { GEOREF_BASE } from './config.js';

// Llena el <select> de provincias con las 24 provincias
export async function cargarProvincias(provinciaSelect, ciudadSelect) {
  try {
    const response = await fetch(`${GEOREF_BASE}/provincias?campos=nombre&orden=nombre&max=24`);
    const data = await response.json();

    provinciaSelect.innerHTML = '<option value="">Seleccioná una provincia</option>';
    data.provincias.forEach((provincia) => {
      const option = document.createElement('option');
      option.value = provincia.nombre;
      option.textContent = provincia.nombre;
      provinciaSelect.appendChild(option);
    });

    provinciaSelect.disabled = false;
  } catch (error) {
    provinciaSelect.innerHTML = '<option value="">No se pudieron cargar las provincias</option>';
    console.error('Error al cargar provincias desde Georef:', error);
  }
}

// Llena el <select> de ciudades según la provincia elegida
export async function cargarCiudades(nombreProvincia, ciudadSelect) {
  ciudadSelect.disabled = true;
  ciudadSelect.innerHTML = '<option value="">Cargando ciudades...</option>';

  try {
    const url = `${GEOREF_BASE}/municipios?provincia=${encodeURIComponent(nombreProvincia)}&campos=nombre&orden=nombre&max=300`;
    const response = await fetch(url);
    const data = await response.json();

    ciudadSelect.innerHTML = '<option value="">Seleccioná una ciudad</option>';
    data.municipios.forEach((municipio) => {
      const option = document.createElement('option');
      option.value = municipio.nombre;
      option.textContent = municipio.nombre;
      ciudadSelect.appendChild(option);
    });

    ciudadSelect.disabled = false;
  } catch (error) {
    ciudadSelect.innerHTML = '<option value="">No se pudieron cargar las ciudades</option>';
    console.error('Error al cargar ciudades desde Georef:', error);
  }
}
