package com.example.caseservice.infrastructure.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Guard-Controller für API-Pfade unter /api.
 *
 * Zweck:
 * - Fängt alle nicht existierenden API-Endpunkte unter /api/** ab
 * - Liefert eine saubere, konsistente 404-Antwort für API-Clients
 *
 * Motivation:
 * - Verhindert irreführende "static resource not found"-Fehler
 * - Vermeidet 500er durch falsch geroutete Requests
 * - Sorgt für ein produktionstaugliches Verhalten der API
 *
 * Hinweis:
 * - Dieser Controller greift nur für Pfade unter /api
 * - Frontend-Routen (React Router) sind davon nicht betroffen
 */
@RestController
@RequestMapping("/api")
public class ApiPrefixGuardController {

    /**
     * Fallback für unbekannte API-Endpunkte.
     *
     * Verhalten:
     * - Gibt immer HTTP 404 (Not Found) zurück
     * - Nutzt ProblemDetail (RFC 7807) für strukturierte Fehlermeldungen
     * - Liefert zusätzliche Metadaten (Pfad, Doku-Hinweis)
     */
    @RequestMapping("/**")
    public ResponseEntity<ProblemDetail> handleUnknownApiPath(HttpServletRequest request) {

        // Standardisiertes Fehlerobjekt nach RFC 7807
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);

        // Kurzbeschreibung des Fehlers
        pd.setTitle("Not found");

        // Detailbeschreibung für API-Consumer
        pd.setDetail("The requested endpoint does not exist. Please refer to the API documentation.");

        // Zusatzinformationen für Debugging und Client-Feedback
        pd.setProperty("path", request.getRequestURI());
        pd.setProperty("documentation", "/swagger-ui.html");

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(pd);
    }
}
