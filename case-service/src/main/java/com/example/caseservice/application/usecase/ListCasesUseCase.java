// Pfad: src/main/java/com/example/caseservice/application/usecase/ListCasesUseCase.java
package com.example.caseservice.application.usecase;

import com.example.caseservice.application.dto.CaseResponse;
import com.example.caseservice.domain.repo.CaseRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ListCasesUseCase {

    // Repository-Abstraktion für Lesezugriffe auf Cases
    private final CaseRepository caseRepository;

    public ListCasesUseCase(CaseRepository caseRepository) {
        this.caseRepository = caseRepository;
    }

    /**
     * Liefert eine Liste von Cases, optional gefiltert nach Status und/oder Priorität.
     *
     * Responsibilities:
     * - Übergibt Filterparameter unverändert an das Repository
     * - Holt die Domain-Entities aus der Persistenz
     * - Mappt die Ergebnisse auf Response-DTOs für die API
     *
     * Hinweis:
     * - Filter sind optional, um flexible Queries zu ermöglichen
     * - Die fachliche Interpretation der Filter (z. B. gültige Statuswerte)
     *   liegt im Repository bzw. vorgelagerten Validierungen
     */
    public List<CaseResponse> execute(Optional<String> status, Optional<String> priority) {
        return caseRepository.findAll(status, priority).stream()
                // Mapping Domain → DTO für die REST-API
                .map(c -> new CaseResponse(
                        c.getId(),
                        c.getTitle(),
                        c.getDescription(),
                        c.getStatus(),
                        c.getPriority(),
                        c.getAssigneeId(),
                        c.getCreatedAt(),
                        c.getUpdatedAt()
                ))
                .toList();
    }
}
