// Pfad: src/api/casesApi.js

// Base-Prefix für alle API-Calls im Frontend.
// Hintergrund: In Vite/Dev-Setup wird /api typischerweise per Proxy an das Backend weitergeleitet.
const API_BASE = '/api'

// Standard-Header für JSON Requests (POST/PATCH).
// Wird zentral definiert, damit es nicht in jeder Funktion wiederholt werden muss.
const JSON_HEADERS = {
  'Content-Type': 'application/json'
}

/**
 * Versucht JSON aus einer Response zu lesen, ohne harte Exceptions zu werfen.
 * - Einige Endpunkte liefern evtl. leeren Body (z. B. 204) oder plain text.
 * - Für Fehlerfälle kann der Body auch mal kein JSON sein.
 */
async function parseJsonSafely(response) {
  const text = await response.text()
  if (!text) return null

  try {
    return JSON.parse(text)
  } catch {
    // Wenn kein gültiges JSON geliefert wurde, geben wir bewusst null zurück.
    return null
  }
}

/**
 * Baut ein Error-Objekt mit HTTP-Status + optionalen Details (z. B. Validation-Errors).
 * So kann die UI später gezielt auf response.status reagieren (Toast/Redirect/etc.).
 */
function createApiError(status, message, details) {
  const err = new Error(message)
  err.status = status
  err.details = details ?? null
  return err
}

/**
 * Zentraler Request-Wrapper für alle API Calls.
 * Vorteile:
 * - Einheitliche Fehlerbehandlung (404/400/409 + Default)
 * - Robustes JSON-Parsing (auch bei leerem Body)
 * - Rückgabe von { response, data }, falls z. B. Header (Location) gebraucht werden
 */
async function request(path, options = {}) {
  const response = await fetch(path, options)

  // Erfolgsfall: JSON (falls vorhanden) lesen und zurückgeben.
  if (response.ok) {
    const data = await parseJsonSafely(response)
    return { response, data }
  }

  // Fehlerfall: Details aus Response lesen (wenn Backend strukturierte Fehler liefert).
  const details = await parseJsonSafely(response)

  // Explizite Behandlung typischer Business-/Validierungsfälle.
  // So kann die UI konsistent passende Meldungen anzeigen.
  if (response.status === 404) throw createApiError(404, 'Not found', details)
  if (response.status === 400) throw createApiError(400, 'Bad request', details)
  if (response.status === 409) throw createApiError(409, 'Conflict', details)

  // Fallback: Alle anderen Fehler (z. B. 500, 503, etc.)
  throw createApiError(response.status, 'Request failed', details)
}

/**
 * GET /cases?status=&priority=
 *
 * Wichtig: Im Frontend rufen wir /api/... auf, damit
 * - React Router Routen wie /cases NICHT vom Proxy "geklaut" werden
 * - Vite nur API Calls an das Backend weiterleitet
 *
 * Parameter:
 * - status, priority sind optional und werden nur gesetzt, wenn vorhanden.
 */
export async function listCases({ status, priority } = {}) {
  const params = new URLSearchParams()

  // Nur vorhandene Filter in die Query übernehmen, damit die URL sauber bleibt.
  if (status) params.set('status', status)
  if (priority) params.set('priority', priority)

  // Querystring nur anhängen, wenn wirklich Parameter gesetzt sind.
  const suffix = params.toString() ? `?${params.toString()}` : ''
  const { data } = await request(`${API_BASE}/cases${suffix}`)

  // Defensive: Wir erwarten ein Array – falls Backend etwas anderes liefert, geben wir ein leeres Array zurück.
  return Array.isArray(data) ? data : []
}

/**
 * GET /cases/{id}
 *
 * Lädt einen einzelnen Case anhand seiner ID.
 * Wir validieren hier minimal (id vorhanden), um offensichtliche Frontend-Bugs früh zu sehen.
 */
export async function getCaseById(id) {
  if (!id) throw createApiError(400, 'Case id is required')

  // encodeURIComponent verhindert Probleme mit Sonderzeichen in der ID.
  const { data } = await request(`${API_BASE}/cases/${encodeURIComponent(id)}`)
  return data
}

/**
 * POST /cases
 * Response: 201 + Location header + body (CaseResponse)
 *
 * title / priority sind Pflichtfelder (Businessregel liegt im Backend).
 * description ist optional und wird als null gesendet, wenn leer.
 */
export async function createCase({ title, description, priority }) {
  const payload = {
    title,
    // Einheitlich null statt "" speichern, damit Backend/DB konsistent bleiben.
    description: description ? description : null,
    priority
  }

  const { response, data } = await request(`${API_BASE}/cases`, {
    method: 'POST',
    headers: JSON_HEADERS,
    body: JSON.stringify(payload)
  })

  // Location Header ist optional, aber praktisch, um direkt zur neuen Ressource zu navigieren.
  // Wir lesen beide Varianten, weil Header je nach Umgebung/Server casing variieren kann.
  const location = response.headers.get('Location') || response.headers.get('location')
  return { location, caseData: data }
}

/**
 * PATCH /cases/{id}/status
 *
 * Setzt den Status eines Cases.
 * Hinweis: Ob ein Status-Übergang erlaubt ist (z. B. OPEN -> DONE), entscheidet das Backend (409 Conflict).
 */
export async function updateCaseStatus(id, status) {
  if (!id) throw createApiError(400, 'Case id is required')
  if (!status) throw createApiError(400, 'Status is required')

  const { data } = await request(`${API_BASE}/cases/${encodeURIComponent(id)}/status`, {
    method: 'PATCH',
    headers: JSON_HEADERS,
    body: JSON.stringify({ status })
  })

  return data
}
