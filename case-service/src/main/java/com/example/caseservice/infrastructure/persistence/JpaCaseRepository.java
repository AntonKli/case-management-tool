package com.example.caseservice.infrastructure.persistence;

import com.example.caseservice.domain.model.Case;
import com.example.caseservice.domain.model.CaseStatus;
import com.example.caseservice.domain.model.Priority;
import com.example.caseservice.domain.repo.CaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaCaseRepository implements CaseRepository {

    // Spring Data Repository (technisches Detail), wird hier gekapselt
    private final SpringDataCaseJpaRepository jpa;

    public JpaCaseRepository(SpringDataCaseJpaRepository jpa) {
        this.jpa = jpa;
    }

    /**
     * Speichert einen Case in der DB.
     *
     * Responsibilities:
     * - Mappt Domain-Entity -> JPA-Entity
     * - Delegiert den Speichervorgang an Spring Data
     * - Mappt die gespeicherte Entity zurück in die Domain
     *
     * Hinweis:
     * - Status/Priority werden als String gespeichert (Enum.name()),
     *   um Domain-Enums unabhängig von JPA zu halten.
     */
    @Override
    public Case save(Case caze) {
        // Domain -> Persistence
        CaseEntity entity = new CaseEntity(
                caze.getId(),
                caze.getTitle(),
                caze.getDescription(),
                caze.getStatus().name(),
                caze.getPriority().name(),
                caze.getAssigneeId(),
                caze.getCreatedAt(),
                caze.getUpdatedAt()
        );

        // Persistieren über Spring Data
        CaseEntity saved = jpa.save(entity);

        // Persistence -> Domain
        return map(saved);
    }

    /**
     * Lädt einen Case anhand seiner ID.
     *
     * Hinweis:
     * - Rückgabe als Optional, damit UseCases sauber entscheiden können
     *   (z. B. 404 werfen, wenn leer).
     */
    @Override
    public Optional<Case> findById(UUID id) {
        return jpa.findById(id).map(this::map);
    }

    /**
     * Liefert eine Liste von Cases, optional gefiltert nach Status und/oder Priorität.
     *
     * Implementationsdetail:
     * - Wir nutzen unterschiedliche Spring-Data-Queries abhängig davon,
     *   welche Filter gesetzt sind.
     * - Sortierung erfolgt absteigend nach createdAt für "neueste zuerst".
     */
    @Override
    public List<Case> findAll(Optional<String> status, Optional<String> priority) {
        List<CaseEntity> entities;

        // Kombinierte Filter: Status + Priority
        if (status.isPresent() && priority.isPresent()) {
            entities = jpa.findByStatusAndPriorityOrderByCreatedAtDesc(status.get(), priority.get());
        }
        // Nur Status
        else if (status.isPresent()) {
            entities = jpa.findByStatusOrderByCreatedAtDesc(status.get());
        }
        // Nur Priority
        else if (priority.isPresent()) {
            entities = jpa.findByPriorityOrderByCreatedAtDesc(priority.get());
        }
        // Keine Filter: alle Cases
        else {
            entities = jpa.findAllByOrderByCreatedAtDesc();
        }

        // Persistenz-Entities zurück in Domain-Objekte mappen
        return entities.stream().map(this::map).toList();
    }

    /**
     * Mapping Persistence -> Domain.
     *
     * Hinweis:
     * - valueOf(...) erwartet gültige Enum-Namen.
     * - Da wir beim Speichern Enum.name() verwenden, ist das konsistent.
     */
    private Case map(CaseEntity saved) {
        return new Case(
                saved.getId(),
                saved.getTitle(),
                saved.getDescription(),
                CaseStatus.valueOf(saved.getStatus()),
                Priority.valueOf(saved.getPriority()),
                saved.getAssigneeId(),
                saved.getCreatedAt(),
                saved.getUpdatedAt()
        );
    }
}
