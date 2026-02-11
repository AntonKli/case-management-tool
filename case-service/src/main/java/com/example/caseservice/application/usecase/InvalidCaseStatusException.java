package com.example.caseservice.application.usecase;

/**
 * Exception für ungültige Case-Statuswerte.
 *
 * Zweck:
 * - Wird geworfen, wenn ein übergebener Statuswert nicht
 *   in das erwartete Status-Enum gemappt werden kann
 *
 * Typischer Einsatz:
 * - Beim Verarbeiten von Status-Updates aus externen Requests
 * - Als Schutzschicht zwischen API-Eingabe (String) und Domain-Enum
 *
 * Hinweis:
 * - Diese Exception wird in der Regel im Controller
 *   in einen HTTP 400 (Bad Request) übersetzt
 */
public class InvalidCaseStatusException extends RuntimeException {

    // Übergibt den fehlerhaften Statuswert zur besseren Fehlersuche
    public InvalidCaseStatusException(String value) {
        super("Invalid case status: " + value);
    }
}
