import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'

import App from './App.jsx'
import './index.css'

// Einstiegspunkt der React-Anwendung.
// Hier wird die App initialisiert und in das DOM gemountet.
createRoot(document.getElementById('root')).render(
  // React StrictMode:
  // - Aktiviert zusätzliche Checks und Warnungen in der Entwicklung
  // - Hilft dabei, potenzielle Side-Effects und unsaubere Patterns früh zu erkennen
  <StrictMode>
    {/* BrowserRouter stellt Client-Side-Routing für die gesamte App bereit */}
    <BrowserRouter>
      {/* Root-Komponente der Anwendung */}
      <App />
    </BrowserRouter>
  </StrictMode>
)
