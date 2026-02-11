package com.example.caseservice.application.dto;

import com.example.caseservice.domain.model.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO für das Erstellen eines neuen Cases (Request Body).
 *
 * Zweck:
 * - Kapselt die Eingabedaten für den Create-Case UseCase
 * - Definiert klare Validierungsregeln für externe Clients (z. B. Frontend)
 *
 * Hinweise:
 * - Wird ausschließlich für eingehende Requests verwendet
 * - Validierung erfolgt über Jakarta Bean Validation
 * - Businessregeln (z. B. Initialstatus) liegen nicht hier, sondern im Domain-/Application-Layer
 */
public record CreateCaseRequest(
        // Titel ist ein Pflichtfeld und darf nicht nur aus Leerzeichen bestehen
        // Maximale Länge wird serverseitig erzwungen
        @NotBlank
        @Size(max = 200)
        String title,

        // Optionale Beschreibung mit begrenzter Länge
        @Size(max = 4000)
        String description,

        // Priorität ist verpflichtend und muss einem gültigen Enum-Wert entsprechen
        @NotNull
        Priority priority
) {}
