function updateNavigation() {
    const token = localStorage.getItem('token');

    document.querySelectorAll('[data-auth-required]').forEach((link) => {
        const item = link.closest('li');
        if (item) item.hidden = !token;
    });

    document.querySelectorAll('[data-login-link]').forEach((link) => {
        const item = link.closest('li');
        if (item) item.hidden = Boolean(token);
    });

    document.querySelectorAll('[data-logout-link]').forEach((link) => {
        const item = link.closest('li');
        if (item) item.hidden = !token;
    });
}

document.addEventListener('DOMContentLoaded', () => {
    updateNavigation();

    document.querySelectorAll('[data-logout-link]').forEach((link) => {
        link.addEventListener('click', () => {
            localStorage.removeItem('token');
            updateNavigation();
            window.location.href = 'index.html#login';
        });
    });

    const nav = document.querySelector('.nav');
    const toggle = document.querySelector('.nav-toggle');
    if (nav && toggle) {
        toggle.addEventListener('click', () => {
            const open = nav.classList.toggle('nav--open');
            toggle.setAttribute('aria-expanded', String(open));
        });
    }
});
