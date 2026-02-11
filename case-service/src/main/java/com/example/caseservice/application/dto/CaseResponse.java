package com.example.caseservice.application.dto;

import com.example.caseservice.domain.model.CaseStatus;
import com.example.caseservice.domain.model.Priority;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO für die API-Antworten rund um einen Case.
 *
 * Zweck:
 * - Repräsentiert die finale, lesbare Sicht auf einen Case für das Frontend
 * - Entkoppelt Domain-Modelle von der REST-API
 *
 * Hinweise:
 * - Wird ausschließlich für Responses verwendet (kein Write-DTO)
 * - Enthält nur Daten, keine Businesslogik
 * - Verwendet Java Records für Immutability und klare Semantik
 */
public record CaseResponse(
        // Eindeutige technische ID des Cases
        UUID id,

        // Kurzer, fachlicher Titel des Cases
        String title,

        // Optionale Beschreibung (kann null sein)
        String description,

        // Aktueller Status des Cases (Domain-Enum)
        CaseStatus status,

        // Priorität des Cases (Domain-Enum)
        Priority priority,

        // Optionaler Bearbeiter (zukünftig für Assignments gedacht)
        UUID assigneeId,

        // Zeitpunkt der Erstellung (serverseitig gesetzt)
        Instant createdAt,

        // Zeitpunkt der letzten Änderung (z. B. Status-Update)
        Instant updatedAt
) {}
