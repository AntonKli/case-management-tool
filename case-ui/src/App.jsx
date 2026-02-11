import { NavLink, Route, Routes } from 'react-router-dom'

import CasesList from './pages/CasesList.jsx'
import CaseDetail from './pages/CaseDetail.jsx'
import CreateCase from './pages/CreateCase.jsx'

/**
 * Root application component.
 *
 * Responsibilities:
 * - Definiert das Grundlayout der Anwendung (Header + Content-Bereich)
 * - Konfiguriert alle Client-Side-Routen
 *
 * Hinweis:
 * - Styling ist hier bewusst minimal gehalten.
 * - Layout, Farben und Abstände kommen aus globalen Styles (index.css),
 *   damit diese Komponente sich auf Struktur und Routing konzentriert.
 */
function App() {
  return (
    <div className="container">
      {/* Globale Kopfzeile der Anwendung */}
      <header className="app-header">
        <h1 className="app-title">Case Management</h1>

        {/* Hauptnavigation */}
        <nav className="row">
          <NavLink
            to="/cases"
            // NavLink liefert isActive, damit wir aktive Routen visuell hervorheben können
            className={({ isActive }) =>
              isActive ? 'nav-link nav-link-active' : 'nav-link'
            }
          >
            Cases
          </NavLink>

          <NavLink
            to="/cases/new"
            className={({ isActive }) =>
              isActive ? 'nav-link nav-link-active' : 'nav-link'
            }
          >
            Create
          </NavLink>
        </nav>
      </header>

      {/* Hauptinhalt: Routing entscheidet, welche Page gerendert wird */}
      <main>
        <Routes>
          {/* Default-Entry (z. B. direkter Aufruf der Root-URL) */}
          <Route path="/" element={<CasesList />} />

          {/* Case-Übersicht */}
          <Route path="/cases" element={<CasesList />} />

          {/* Case anlegen */}
          <Route path="/cases/new" element={<CreateCase />} />

          {/* Case-Detailansicht */}
          <Route path="/cases/:id" element={<CaseDetail />} />

          {/* Fallback für unbekannte Routen */}
          <Route path="*" element={<NotFound />} />
        </Routes>
      </main>
    </div>
  )
}

/**
 * Fallback-Komponente für nicht existierende Routen.
 * Absichtlich sehr simpel gehalten.
 */
function NotFound() {
  return (
    <div>
      <h2>Not Found</h2>
      <p className="muted">Die Seite existiert nicht.</p>
    </div>
  )
}

export default App
