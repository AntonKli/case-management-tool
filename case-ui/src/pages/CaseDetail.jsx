import { useEffect, useMemo, useState } from 'react'
import { Link, useParams } from 'react-router-dom'

import { getCaseById, updateCaseStatus } from '../api/casesApi.js'

function CaseDetail() {
  // Case-ID kommt aus der Route, z. B. /cases/:id
  const { id } = useParams()

  // Geladene Case-Daten vom Backend
  const [caseData, setCaseData] = useState(null)

  // Loading-State für das initiale Laden der Detailseite
  const [loading, setLoading] = useState(true)

  // Separater Loading-State fürs Update, damit die Seite nicht komplett "einfriert"
  // und wir z. B. den Button/Select gezielt deaktivieren können.
  const [loadingUpdate, setLoadingUpdate] = useState(false)

  // Fehlerzustand fürs initiale Laden.
  // Für 404 nutzen wir ein eigenes Flag, um eine dedizierte Not-Found Ansicht zu rendern.
  const [error, setError] = useState(null)

  // Fehler/Success Messages speziell für das Status-Update (UI Feedback)
  const [updateError, setUpdateError] = useState(null)
  const [updateSuccess, setUpdateSuccess] = useState(null)

  // Backend-Enum (verbindlich): OPEN → IN_PROGRESS → DONE → CLOSED
  // useMemo: Liste ist konstant und soll nicht bei jedem Render neu erstellt werden.
  const statusOptions = useMemo(() => ['OPEN', 'IN_PROGRESS', 'DONE', 'CLOSED'], [])

  // Der aktuell ausgewählte Status im UI (Select).
  // Wird beim Laden mit dem Case-Status initialisiert.
  const [nextStatus, setNextStatus] = useState('OPEN')

  useEffect(() => {
    // Guard: verhindert setState nach Unmount (z. B. schnelles Navigieren / Race Conditions).
    let cancelled = false

    async function loadCase() {
      // Reset UI-State bei neuer ID
      setLoading(true)
      setError(null)
      setCaseData(null)

      try {
        const data = await getCaseById(id)
        if (cancelled) return

        // Case-Daten in den State übernehmen
        setCaseData(data)

        // Select auf den aktuellen Server-Status setzen (Fallback: OPEN)
        setNextStatus(data?.status ?? 'OPEN')
      } catch (err) {
        if (cancelled) return

        // 404 speziell behandeln, damit wir eine eigene Ansicht anzeigen können
        if (err?.status === 404) {
          setError('NOT_FOUND')
        } else {
          // Fallback für alle anderen Fehler
          setError(err?.message || 'Failed to load case')
        }
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    loadCase()

    // Cleanup für den Effekt
    return () => {
      cancelled = true
    }
  }, [id])

  async function handleUpdateStatus(e) {
    e.preventDefault()

    // UI Feedback zurücksetzen, bevor wir erneut updaten
    setUpdateError(null)
    setUpdateSuccess(null)

    // Frontend-Minimalcheck (Businessregeln/Transitions bleiben im Backend)
    if (!nextStatus) {
      setUpdateError('Please select a status.')
      return
    }

    setLoadingUpdate(true)
    try {
      // Server ist "Source of truth": wir übernehmen den Response-Case komplett,
      // damit UI sicher den finalen Status zeigt (inkl. evtl. serverseitiger Anpassungen).
      const updated = await updateCaseStatus(id, nextStatus)

      setCaseData(updated)

      // Select synchron halten (Fallback: bisher gewählter Status)
      setNextStatus(updated?.status ?? nextStatus)

      setUpdateSuccess('Status updated.')
    } catch (err) {
      // HTTP-Fehler in verständliche UI-Meldungen übersetzen
      if (err?.status === 404) setUpdateError('Case not found.')
      else if (err?.status === 400) setUpdateError('Invalid status value.')
      else if (err?.status === 409) setUpdateError('Invalid status transition for the current case state.')
      else setUpdateError(err?.message || 'Failed to update status.')
    } finally {
      setLoadingUpdate(false)
    }
  }

  function statusClass(status) {
    // CSS Klassen kommen aus index.css und sind an Domain-Status gekoppelt.
    // So bleibt das Styling zentral und konsistent.
    const map = {
      OPEN: 'status-open',
      IN_PROGRESS: 'status-in-progress',
      DONE: 'status-done',
      CLOSED: 'status-closed'
    }
    return map[status] || ''
  }

  // --- Rendering States (klar und früh returnen) ---

  if (loading) return <p className="muted">Loading case…</p>

  if (error === 'NOT_FOUND') {
    return (
      <div className="container">
        <h2>Case not found</h2>
        <p>
          Es gibt keinen Case mit der ID: <code>{id}</code>
        </p>
        <Link to="/cases">← Back to cases</Link>
      </div>
    )
  }

  if (error) {
    return (
      <div className="container">
        <p className="error">Error: {String(error)}</p>
        <Link to="/cases">← Back to cases</Link>
      </div>
    )
  }

  // Defensive: sollte selten passieren (z. B. wenn Backend null liefert),
  // aber verhindert kaputtes Rendering.
  if (!caseData) {
    return (
      <div className="container">
        <p className="muted">Nothing to display.</p>
        <Link to="/cases">← Back to cases</Link>
      </div>
    )
  }

  return (
    <div className="container">
      <div style={{ marginBottom: 12 }}>
        <Link to="/cases">← Back to cases</Link>
      </div>

      <h2>Case #{caseData.id}</h2>

      <section className="card">
        {/* Anzeige der wichtigsten Felder (Fallback: '-') */}
        <Row label="Title" value={caseData.title ?? '-'} />
        <Row label="Status" value={caseData.status ?? '-'} valueClassName={statusClass(caseData.status)} />
        <Row label="Priority" value={caseData.priority ?? '-'} />
      </section>

      <section className="card" style={{ marginTop: 12 }}>
        <h3>Update status</h3>

        {/* Select + Submit: UI wird bei Update deaktiviert, damit keine Doppel-Requests entstehen */}
        <form onSubmit={handleUpdateStatus} className="row">
          <select value={nextStatus} onChange={(e) => setNextStatus(e.target.value)} disabled={loadingUpdate}>
            {statusOptions.map((s) => (
              <option key={s} value={s}>
                {s}
              </option>
            ))}
          </select>

          <button type="submit" disabled={loadingUpdate}>
            {loadingUpdate ? 'Updating…' : 'Update'}
          </button>
        </form>

        {/* Ergebnis-Feedback nach Update */}
        {updateError ? <p className="error">{updateError}</p> : null}
        {updateSuccess ? <p className="success">{updateSuccess}</p> : null}

        {/* Hinweis für Nutzer: tatsächliche Validierung/Transitions laufen im Backend */}
        <p className="muted">
          Erlaubte Reihenfolge: <code>OPEN → IN_PROGRESS → DONE → CLOSED</code>
        </p>
      </section>
    </div>
  )
}

function Row({ label, value, valueClassName }) {
  // Kleines UI-Helper-Component für 2-Spalten Layout (Label / Value)
  return (
    <div className="grid-2">
      <div className="muted">{label}</div>
      <div className={valueClassName} style={{ fontWeight: 600 }}>
        {value}
      </div>
    </div>
  )
}

export default CaseDetail
