// Pfad: src/main/java/com/example/caseservice/application/usecase/GetCaseUseCase.java
package com.example.caseservice.application.usecase;

import com.example.caseservice.application.dto.CaseResponse;
import com.example.caseservice.domain.exception.CaseNotFoundException;
import com.example.caseservice.domain.model.Case;
import com.example.caseservice.domain.repo.CaseRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetCaseUseCase {

    // Zugriff auf die Persistenz über Repository-Abstraktion
    private final CaseRepository caseRepository;

    public GetCaseUseCase(CaseRepository caseRepository) {
        this.caseRepository = caseRepository;
    }

    /**
     * Liefert einen einzelnen Case anhand seiner ID.
     *
     * Responsibilities:
     * - Lädt den Case aus der Persistenz
     * - Wirft eine fachliche Exception, wenn der Case nicht existiert
     * - Mappt die Domain-Entity auf ein Response-DTO
     *
     * Hinweis:
     * - Die CaseNotFoundException wird typischerweise im Controller
     *   in einen HTTP 404 (Not Found) übersetzt.
     */
    public CaseResponse execute(UUID id) {
        // Repository liefert Optional → bei Nicht-Fund wird eine Domain-Exception geworfen
        Case caze = caseRepository.findById(id)
                .orElseThrow(() -> new CaseNotFoundException(id));

        // Mapping Domain → DTO für die API-Schicht
        return new CaseResponse(
                caze.getId(),
                caze.getTitle(),
                caze.getDescription(),
                caze.getStatus(),
                caze.getPriority(),
                caze.getAssigneeId(),
                caze.getCreatedAt(),
                caze.getUpdatedAt()
        );
    }
}
