package com.example.caseservice.application.usecase;

import com.example.caseservice.application.dto.CaseResponse;
import com.example.caseservice.application.dto.CreateCaseRequest;
import com.example.caseservice.domain.model.Case;
import com.example.caseservice.domain.model.CaseStatus;
import com.example.caseservice.domain.repo.CaseRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class CreateCaseUseCase {

    // Repository-Abstraktion (Persistence ist außerhalb des UseCases versteckt)
    private final CaseRepository caseRepository;

    public CreateCaseUseCase(CaseRepository caseRepository) {
        this.caseRepository = caseRepository;
    }

    /**
     * Erstellt einen neuen Case und gibt die persistierte Version als Response-DTO zurück.
     *
     * Responsibilities:
     * - Minimale Schutzchecks (null / blank title) als defensive Programmierung
     * - Setzt systemseitige Default-Werte (z. B. Status = OPEN, Timestamps)
     * - Delegiert Persistenz an das Repository
     * - Mappt Domain-Entity -> API Response DTO
     *
     * Hinweis:
     * - Bean Validation (@NotBlank, @NotNull) wird typischerweise schon im Controller geprüft.
     * - Die Checks hier sind trotzdem sinnvoll, falls der UseCase direkt (ohne Controller) genutzt wird.
     */
    public CaseResponse execute(CreateCaseRequest request) {
        // Defensive: UseCase soll nie mit null aufgerufen werden.
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }

        // Defensive: Titel darf nicht leer sein (zusätzlicher Schutz neben Bean Validation).
        if (request.title() == null || request.title().trim().isEmpty()) {
            throw new IllegalArgumentException("title must not be blank");
        }

        // Serverzeit wird hier zentral gesetzt, damit Client-Zeiten keinen Einfluss haben.
        Instant now = Instant.now();

        // Domain-Entity erstellen:
        // - ID wird hier erzeugt (UUID)
        // - Initialstatus ist per Businessregel OPEN
        // - Assignee ist optional und beim Create zunächst null
        Case newCase = new Case(
                UUID.randomUUID(),
                request.title().trim(),
                request.description(),
                CaseStatus.OPEN,
                request.priority(),
                null,
                now,
                now
        );

        // Persistieren über Repository (Infrastruktur bleibt vom UseCase getrennt)
        Case saved = caseRepository.save(newCase);

        // Mapping Domain -> DTO:
        // UI bekommt nur die für die API relevanten Werte.
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
}
