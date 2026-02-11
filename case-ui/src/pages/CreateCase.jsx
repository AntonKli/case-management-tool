import { useMemo, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'

import { createCase } from '../api/casesApi.js'

/**
 * Page: CreateCase
 *
 * Responsibilities:
 * - Nimmt User-Eingaben für einen neuen Case entgegen
 * - Macht minimale Client-Validierung (schnelles Feedback)
 * - Ruft das Backend auf und navigiert anschließend zum neu erstellten Case
 *
 * Note:
 * - Backend bleibt "Source of truth" (Businessregeln + endgültige Validierung).
 * - Validation-Fehler werden typischerweise als HTTP 400 zurückgegeben.
 */
function CreateCase() {
  // React Router Navigation nach erfolgreichem Create
  const navigate = useNavigate()

  // Backend Enum (Priority). Explizit im UI halten, um Tippfehler/"Magic Strings" zu vermeiden.
  // useMemo: Liste ist konstant und soll nicht bei jedem Render neu gebaut werden.
  const priorityOptions = useMemo(() => ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'], [])

  // Form State (controlled inputs)
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [priority, setPriority] = useState('MEDIUM')

  // Submit State: verhindert doppelte Requests und erlaubt UI-Deaktivierung
  const [submitting, setSubmitting] = useState(false)

  // Fehler-Message für UI Feedback (z. B. Validation / Netzwerkfehler)
  const [error, setError] = useState(null)

  async function handleSubmit(e) {
    e.preventDefault()
    setError(null)

    // Minimaler Client-Check:
    // - trim() verhindert "nur Leerzeichen"
    // - Backend validiert trotzdem final und liefert bei Problemen 400 zurück.
    const trimmedTitle = title.trim()
    const trimmedDescription = description.trim()

    if (!trimmedTitle) {
      setError('Title is required.')
      return
    }

    // Defensive: UI darf nur Werte senden, die das Backend-Enum kennt.
    if (!priorityOptions.includes(priority)) {
      setError('Priority is invalid.')
      return
    }

    setSubmitting(true)
    try {
      const { location, caseData } = await createCase({
        title: trimmedTitle,
        // Optionales Feld: wir senden null statt "", damit Backend/DB konsistent bleibt.
        description: trimmedDescription ? trimmedDescription : null,
        priority
      })

      // Preferred Redirect:
      // Backend liefert idealerweise "Location: /cases/{id}" (201 Created).
      // Vorteil: das ist die kanonische URL der neuen Ressource.
      if (location) {
        const match = location.match(/\/cases\/([^/]+)$/)
        const idFromLocation = match?.[1]
        if (idFromLocation) {
          navigate(`/cases/${idFromLocation}`)
          return
        }
      }

      // Fallback:
      // Manche Umgebungen/Proxies geben Location Header nicht frei,
      // daher unterstützen wir zusätzlich die ID aus dem Response-Body.
      if (caseData?.id) {
        navigate(`/cases/${caseData.id}`)
        return
      }

      // Worst-Case Fallback: User landet trotzdem auf einer validen Seite.
      navigate('/cases')
    } catch (err) {
      // 400 = Validierungsfehler (User kann Input korrigieren)
      if (err?.status === 400) {
        setError('Validation failed. Please check your input.')
      } else {
        // Alle anderen Fehler sind eher technische Probleme (Netzwerk/Server/etc.)
        setError(err?.message || 'Failed to create case.')
      }
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="container">
      <div className="page-actions">
        <Link to="/cases">← Back to cases</Link>
      </div>

      <h2>Create Case</h2>

      {/* Controlled form: Input-Values kommen aus State, onSubmit triggert Create */}
      <form onSubmit={handleSubmit} className="card form-narrow">
        <div className="stack">
          <label>
            <div className="label-text muted">Title *</div>
            <input
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              // UI-Limit als erstes Guardrail; Backend validiert zusätzlich.
              maxLength={200}
              placeholder="Short summary (max 200)"
              disabled={submitting}
            />
          </label>

          <label>
            <div className="label-text muted">Description</div>
            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              maxLength={4000}
              placeholder="Optional details (max 4000)"
              disabled={submitting}
              rows={6}
            />
          </label>

          <label>
            <div className="label-text muted">Priority *</div>
            <select value={priority} onChange={(e) => setPriority(e.target.value)} disabled={submitting}>
              {priorityOptions.map((p) => (
                <option key={p} value={p}>
                  {p}
                </option>
              ))}
            </select>
          </label>

          <div className="row">
            {/* Während submit: Button deaktivieren, um doppelte Creates zu verhindern */}
            <button type="submit" disabled={submitting}>
              {submitting ? 'Creating…' : 'Create'}
            </button>

            <Link to="/cases">Cancel</Link>
          </div>

          {/* Fehlerausgabe direkt unter dem Formular */}
          {error ? <p className="error">{error}</p> : null}
        </div>
      </form>
    </div>
  )
}

export default CreateCase
