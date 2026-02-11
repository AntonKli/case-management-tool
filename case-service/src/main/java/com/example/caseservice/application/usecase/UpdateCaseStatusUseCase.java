package com.example.caseservice.application.usecase;

import com.example.caseservice.application.dto.CaseResponse;
import com.example.caseservice.application.dto.UpdateCaseStatusRequest;
import com.example.caseservice.domain.exception.CaseNotFoundException;
import com.example.caseservice.domain.exception.InvalidCaseStatusException;
import com.example.caseservice.domain.model.Case;
import com.example.caseservice.domain.model.CaseStatus;
import com.example.caseservice.domain.repo.CaseRepository;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.UUID;

@Service
public class UpdateCaseStatusUseCase {

    // Repository-Abstraktion (Persistenzdetails sind nicht Teil des UseCases)
    private final CaseRepository caseRepository;

    public UpdateCaseStatusUseCase(CaseRepository caseRepository) {
        this.caseRepository = caseRepository;
    }

    /**
     * Aktualisiert den Status eines bestehenden Cases.
     *
     * Responsibilities:
     * - Lädt den bestehenden Case (404, falls nicht vorhanden)
     * - Parst/normalisiert den Statuswert aus dem Request (400 bei ungültigem Wert)
     * - Delegiert die Transition-Regel an die Domain (Case.changeStatusTo)
     * - Persistiert den aktualisierten Case und mappt ihn auf ein Response-DTO
     *
     * Hinweis:
     * - Die eigentlichen Businessregeln der Status-Transitions liegen in der Domain
     *   (z. B. OPEN → IN_PROGRESS → DONE → CLOSED).
     * - Ungültige Übergänge sollen typischerweise als 409 (Conflict) gemappt werden
     *   (abhängig von der Domain-Exception, die changeStatusTo wirft).
     */
    public CaseResponse execute(UUID id, UpdateCaseStatusRequest request) {
        // Case existiert? Wenn nicht: fachliche NotFound-Exception (wird im Controller zu 404)
        Case existing = caseRepository.findById(id)
                .orElseThrow(() -> new CaseNotFoundException(id));

        // Input-String aus dem Request in ein Domain-Enum übersetzen (Source of truth: CaseStatus)
        CaseStatus newStatus = parseStatus(request.status());

        // Statuswechsel über die Domain-Logik durchführen (inkl. Transition-Regeln)
        Case updated = existing.changeStatusTo(newStatus);

        // Persistieren und das gespeicherte Ergebnis zurückgeben (inkl. updatedAt etc.)
        Case saved = caseRepository.save(updated);

        // Mapping Domain → DTO für die API-Schicht
        return new CaseResponse(
                saved.getId(),
                saved.getTitle(),
                saved.getDescription(),
                saved.getStatus(),
                saved.getPriority(),
                saved.getAssigneeId(),
                saved.getCreatedAt(),
                saved.getUpdatedAt()
        );
    }

    /**
     * Parst den Status aus einem String und normalisiert ihn für Enum-Mapping.
     *
     * Akzeptiert z. B.:
     * - "open"
     * - " OPEN "
     * - "in_progress"
     *
     * Wirft InvalidCaseStatusException, wenn der Wert fehlt oder nicht gemappt werden kann.
     */
    private static CaseStatus parseStatus(String raw) {
        // Defensive: leerer/fehlender Status ist ein ungültiger Request
        if (raw == null || raw.isBlank()) {
            throw new InvalidCaseStatusException(raw == null ? "<null>" : raw);
        }

        // Normalisierung: trim + Uppercase (Locale.ROOT für stabile Ergebnisse unabhängig von Sprache/Locale)
        String normalized = raw.trim().toUpperCase(Locale.ROOT);

        try {
            return CaseStatus.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            // Wert ist nicht Teil des Enums → 400 Bad Request
            throw new InvalidCaseStatusException(raw);
        }
    }
}
