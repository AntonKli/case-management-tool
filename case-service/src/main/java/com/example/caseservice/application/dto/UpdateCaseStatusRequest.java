package com.example.caseservice.application.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO für das Aktualisieren des Case-Status (Request Body).
 *
 * Zweck:
 * - Kapselt den neuen Statuswert für ein bestehendes Case
 * - Wird vom Controller an den entsprechenden UseCase weitergereicht
 *
 * Hinweise:
 * - Validiert nur das Vorhandensein des Status-Wertes
 * - Die fachliche Prüfung (gültiger Enum-Wert, erlaubte Status-Transition)
 *   erfolgt im Application- bzw. Domain-Layer
 */
public record UpdateCaseStatusRequest(
        // Neuer Status als String (z. B. "OPEN", "IN_PROGRESS", ...)
        // Enum-Mapping und Transition-Regeln liegen bewusst nicht im DTO
        @NotBlank
        String status
) {}
