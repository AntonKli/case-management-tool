package com.example.caseservice.domain.exception;

import com.example.caseservice.domain.model.CaseStatus;

import java.util.UUID;

/**
 * Domain-Exception für ungültige Status-Übergänge eines Cases.
 *
 * Zweck:
 * - Signalisiert einen fachlichen Konflikt, wenn ein Case
 *   einen Statuswechsel durchführen soll, der laut Businessregeln
 *   nicht erlaubt ist
 *
 * Typischer Einsatz:
 * - Wird direkt in der Domain (z. B. Case.changeStatusTo)
 *   ausgelöst, wenn eine Transition nicht zulässig ist
 *
 * Beispiele:
 * - OPEN → DONE (überspringt IN_PROGRESS)
 * - CLOSED → IN_PROGRESS (Rücksprung nicht erlaubt)
 *
 * Hinweis:
 * - Diese Exception steht für einen fachlichen Konflikt
 * - In der REST-Schicht wird sie üblicherweise
 *   auf HTTP 409 (Conflict) gemappt
 */
public class InvalidCaseStatusTransitionException extends RuntimeException {

    // Enthält alle relevanten Informationen für Debugging und Logs:
    // Case-ID sowie alter und neuer Status
    public InvalidCaseStatusTransitionException(UUID id, CaseStatus from, CaseStatus to) {
        super("Invalid status transition for case " + id + ": " + from + " -> " + to);
    }
}
