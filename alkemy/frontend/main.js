
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

  const startButton = document.querySelector('.btn--primary');

  if (startButton) {
    startButton.addEventListener('click', () => {
      console.log('Botón "Empezar ahora" clickeado');
      // Acá se rederige a la sección de registro/login
    });
  }
});