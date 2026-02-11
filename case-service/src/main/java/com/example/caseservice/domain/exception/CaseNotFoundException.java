package com.example.caseservice.domain.exception;

import java.util.UUID;

/**
 * Domain-Exception für den Fall, dass ein Case nicht existiert.
 *
 * Zweck:
 * - Signalisiert eindeutig, dass ein Case mit der angegebenen ID
 *   fachlich nicht gefunden wurde
 *
 * Typischer Einsatz:
 * - Beim Laden eines einzelnen Cases (Read / Update)
 * - Wird vom Repository oder UseCase geworfen, wenn keine Entität existiert
 *
 * Hinweis:
 * - Diese Exception wird in der REST-Schicht typischerweise
 *   in einen HTTP 404 (Not Found) übersetzt
 * - Bewusst als RuntimeException umgesetzt, um den UseCase-Code schlank zu halten
 */
public class CaseNotFoundException extends RuntimeException {

    // Übergibt die gesuchte Case-ID für bessere Debug- und Log-Ausgaben
    public CaseNotFoundException(UUID id) {
        super("Case not found: " + id);
    }
}
