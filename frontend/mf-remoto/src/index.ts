// Frontera asíncrona requerida por Module Federation: garantiza que los módulos
// compartidos (react, mui, emotion) se inicialicen antes de arrancar la app standalone.
import('./bootstrap');

export {};
