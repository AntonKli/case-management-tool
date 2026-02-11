package com.example.caseservice.domain.exception;

/**
 * Domain-Exception für ungültige Case-Statuswerte.
 *
 * Zweck:
 * - Signalisiert, dass ein übergebener Statuswert
 *   nicht Teil des gültigen Domain-Status-Enums ist
 *
 * Typischer Einsatz:
 * - Beim Parsen externer Eingaben (z. B. REST Requests)
 * - Als Schutzschicht zwischen String-Werten und Domain-Enums
 *
 * Hinweis:
 * - Diese Exception beschreibt einen fachlich ungültigen Input
 * - In der REST-Schicht wird sie üblicherweise
 *   in einen HTTP 400 (Bad Request) übersetzt
 */
public class InvalidCaseStatusException extends RuntimeException {

    // Übergibt den ungültigen Statuswert zur besseren Diagnose
    public InvalidCaseStatusException(String status) {
        super("Invalid case status: " + status);
    }
}
