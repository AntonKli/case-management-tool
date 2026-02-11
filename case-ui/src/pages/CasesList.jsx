import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'

import { listCases } from '../api/casesApi.js'

function CasesList() {
  // Liste der Cases aus dem Backend (Default: leeres Array für stabiles Rendering)
  const [cases, setCases] = useState([])

  // Loading-State für initialen Fetch
  const [loading, setLoading] = useState(true)

  // Fehler-State (z. B. Netzwerkfehler / 5xx / unerwartete Response)
  const [error, setError] = useState(null)

  useEffect(() => {
    // Guard: verhindert setState nach Unmount (z. B. wenn der User während eines Requests navigiert).
    let cancelled = false

    async function loadCases() {
      // Bei Reload/reset den UI-State sauber setzen
      setLoading(true)
      setError(null)

      try {
        const data = await listCases()

        // State nur setzen, wenn Component noch gemounted ist
        if (!cancelled) setCases(data)
      } catch (err) {
        // Fallback-Message, falls kein err.message vorhanden ist
        if (!cancelled) setError(err?.message || 'Failed to load cases')
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    loadCases()

    // Cleanup: markiert Request als "irrelevant", falls die Component unmounted
    return () => {
      cancelled = true
    }
  }, [])

  function statusClass(status) {
    // Backend-Status → CSS-Klasse
    // Farben/Styles liegen zentral in index.css, damit UI konsistent bleibt.
    const map = {
      OPEN: 'status-open',
      IN_PROGRESS: 'status-in-progress',
      DONE: 'status-done',
      CLOSED: 'status-closed'
    }
    return map[status] || ''
  }

  // --- Rendering States (früh returnen, damit JSX übersichtlich bleibt) ---

  if (loading) {
    return (
      <div className="container">
        <p className="muted">Loading cases…</p>
      </div>
    )
  }

  if (error) {
    return (
      <div className="container">
        <h2>Cases</h2>
        <p className="error">Error: {error}</p>
      </div>
    )
  }

  // Empty-State: keine Daten vorhanden (z. B. frische DB)
  if (!cases || cases.length === 0) {
    return (
      <div className="container">
        <h2>Cases</h2>
        <p className="muted">No cases found.</p>
        <Link to="/cases/new">Create first case</Link>
      </div>
    )
  }

  return (
    <div className="container">
      <h2>Cases</h2>

      <div className="card">
        <table className="table">
          <thead>
            <tr>
              <th className="muted">ID</th>
              <th className="muted">Title</th>
              <th className="muted">Status</th>
              <th className="muted">Priority</th>
            </tr>
          </thead>

          <tbody>
            {cases.map((c) => (
              // Key ist wichtig für React Rendering/Updates (hier: eindeutige Case-ID)
              <tr key={c.id}>
                <td>
                  {/* Link zur Detailseite */}
                  <Link to={`/cases/${c.id}`}>{c.id}</Link>
                </td>

                <td>{c.title}</td>

                {/* Status farblich hervorheben über CSS-Klasse */}
                <td className={statusClass(c.status)}>{c.status}</td>

                <td>{c.priority}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}

export default CasesList
