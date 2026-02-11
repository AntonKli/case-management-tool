package com.example.caseservice.infrastructure.web;

import java.time.Instant;
import java.util.Map;

/**
 * Einheitliches Error-Response-Modell für die REST-API.
 *
 * Zweck:
 * - Stellt ein konsistentes Fehlerformat für alle API-Endpunkte bereit
 * - Erleichtert das Handling von Fehlern im Frontend
 *
 * Typische Verwendung:
 * - In @ControllerAdvice / Exception-Handlern
 * - Als Response-Body für 4xx / 5xx Fehler
 */
public record ApiError(
        // Zeitpunkt, zu dem der Fehler erzeugt wurde
        Instant timestamp,

        // Kurzbeschreibung des Fehlertyps (z. B. "Bad Request", "Not Found")
        String error,

        // Menschenlesbare Fehlermeldung
        String message,

        // Optionale Zusatzinformationen (z. B. Validierungsdetails)
        Map<String, Object> details
) {

    /**
     * Factory-Methode für einfache Fehler ohne Detaildaten.
     */
    public static ApiError of(String error, String message) {
        return new ApiError(Instant.now(), error, message, Map.of());
    }

    /**
     * Factory-Methode für Fehler mit zusätzlichen Details.
     */
    public static ApiError of(String error, String message, Map<String, Object> details) {
        return new ApiError(Instant.now(), error, message, details);
    }
}
