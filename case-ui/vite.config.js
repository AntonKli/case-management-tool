import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  // React Plugin für Vite (JSX, Fast Refresh, etc.)
  plugins: [react()],

  server: {
    // Dev-Server Konfiguration
    proxy: {
      /**
       * Proxy für API-Requests während der Entwicklung.
       *
       * Ziel:
       * - Verhindert CORS-Probleme zwischen Frontend (Vite) und Backend (Spring Boot)
       * - Trennt klar React-Routen (/cases, /cases/:id, ...) von Backend-Endpunkten
       *
       * Beispiel:
       *   Frontend ruft auf:   GET /api/cases
       *   Vite leitet weiter:  http://localhost:8082/cases
       */
      '/api': {
        // Backend-Base-URL (Spring Boot)
        target: 'http://localhost:8082',

        // Passt den Origin-Header an, damit das Backend den Request akzeptiert
        changeOrigin: true,

        // Entfernt das /api Prefix, da das Backend es nicht kennt
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
})
