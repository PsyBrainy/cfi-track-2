// nav.js — comportamiento del menú de navegación (hamburguesa responsive).

export function initNav() {
  const navToggle = document.querySelector('.nav-toggle');
  const nav = document.querySelector('.nav');

  if (!navToggle || !nav) return;

  navToggle.addEventListener('click', () => {
    const isOpen = nav.classList.toggle('nav--open');
    navToggle.setAttribute('aria-expanded', isOpen);
  });

  // Cerrar el menú al hacer click en un link
  nav.querySelectorAll('a').forEach((link) => {
    link.addEventListener('click', () => {
      nav.classList.remove('nav--open');
      navToggle.setAttribute('aria-expanded', 'false');
    });
  });
}
