// js/footer-loader.js
import { BaseUrl } from './config.js';

// 🎯 CORRECCIÓN QUIRÚRGICA: Declaramos explícitamente la constante con const al inicio del archivo
const URL_LOCAL_DOLAR = `${BaseUrl}/api/public/dolar-rates`;

document.addEventListener('DOMContentLoaded', () => {
    // 🛡️ REGLA DE EXCLUSIÓN: Si es una ruta de admin, el footer no se genera
    if (window.location.pathname.includes('admin')) {
        console.log('💼 Ruta de administración de Alkywall detectada. Se omite el Footer global.');
        return;
    }

    inyectarFooterGlobal();
});

function inyectarFooterGlobal() {
    const footerContainer = document.getElementById('global-footer');
    if (!footerContainer) return;

    footerContainer.innerHTML = `
        <footer class="alky-global-footer">
            <div class="alky-footer-container">
                <div class="alky-footer-grid">
                    
                    <!-- Columna 1: Branding y Estado del Servidor -->
                    <div class="alky-footer-brand">
                        <span class="alky-footer-logo">Alkywall</span>
                        <p class="alky-footer-tagline">Monitoreá tus activos financieros en tiempo real de forma segura y transparente.</p>
                    </div>

                    <!-- Columna 2: Enlaces Rápidos de Soporte -->
                    <div class="alky-footer-links">
                        <h4>Soporte y Legales</h4>
                        <ul>
                            <li><a href="#ayuda">Centro de Ayuda</a></li>
                            <li><a href="#terminos">Términos del Servicio</a></li>
                            <li><a href="#privacidad">Privacidad de Datos</a></li>
                            <li><a href="#defensa">Defensa del Consumidor</a></li>
                        </ul>
                    </div>

                    <!-- Columna 3: Ticker del Mercado en Vivo -->
                    <div class="alky-footer-market">
                        <h4>Cotizaciones</h4>
                        <div class="alky-footer-ticker" id="footer-dolar-ticker">
                            <span class="ticker-loading-text">Sincronizando pizarras...</span>
                        </div>
                    </div>

                </div>

                <!-- Línea de Cierre Legal -->
                <div class="alky-footer-bottom">
                    <p>&copy; 2026 Alkywall. Todos los derechos reservados. Regulado por estándares internacionales de billeteras virtuales.</p>
                </div>
            </div>
        </footer>
    `;

    // Disparamos la carga de datos financieros una vez construido el HTML básico
    obtenerCotizacionesParaFooter();
}

// Dentro de tu js/footer-loader.js (Reemplazar esta función)
async function obtenerCotizacionesParaFooter() {
    const tickerContainer = document.getElementById('footer-dolar-ticker');
    if (!tickerContainer) return;

    try {
        const response = await fetch(URL_LOCAL_DOLAR);
        if (!response.ok) throw new Error('Error al conectar con el servidor local');
        
        const datos = await response.json(); 
        
        const oficial = datos.find(d => d.casa === 'oficial');
        const blue = datos.find(d => d.casa === 'blue');

        if (oficial && blue) {
            tickerContainer.innerHTML = `
                <div class="alky-premium-ticker-box">
                    
                    <!-- Tarjeta Dólar Oficial -->
                    <div class="market-card-row market-card-row--oficial">
                        <div class="market-card-badge-group">
                            <span class="market-card-badge">OFICIAL</span>
                            <span class="market-trend-arrow">▲</span>
                        </div>
                        <div class="market-card-rates">
                            <div class="rate-sub">Compra <span class="rate-num">$${oficial.compra}</span></div>
                            <div class="rate-sub">Venta <span class="rate-num-accent">$${oficial.venta}</span></div>
                        </div>
                    </div>

                    <!-- Tarjeta Dólar Blue -->
                    <div class="market-card-row market-card-row--blue">
                        <div class="market-card-badge-group">
                            <span class="market-card-badge market-card-badge--blue">BLUE</span>
                            <span class="market-trend-arrow market-trend-arrow--blue">▲</span>
                        </div>
                        <div class="market-card-rates">
                            <div class="rate-sub">Compra <span class="rate-num">$${blue.compra}</span></div>
                            <div class="rate-sub">Venta <span class="rate-num-blue">$${blue.venta}</span></div>
                        </div>
                    </div>

                </div>
            `;
        }
    } catch (error) {
        console.error('Error al renderizar el ticker premium:', error);
        tickerContainer.innerHTML = `<span class="ticker-error-text" style="color: #64748b; font-size: 0.85rem;">Servicio de cotizaciones demorado</span>`;
    }
}

