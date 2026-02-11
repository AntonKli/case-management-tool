package com.example.caseservice.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA Repository für CaseEntity.
 *
 * Zweck:
 * - Stellt CRUD-Operationen für CaseEntity bereit (über JpaRepository)
 * - Enthält zusätzliche Query-Methoden für die Case-Liste inkl. Filter/Sortierung
 *
 * Hinweise:
 * - Gehört bewusst zur Infrastruktur/Persistence-Schicht
 * - Die Domain kennt dieses Interface nicht (sie nutzt nur CaseRepository)
 * - Query-Methoden nutzen Spring Data Naming Conventions (keine eigene SQL nötig)
 */
public interface SpringDataCaseJpaRepository extends JpaRepository<CaseEntity, UUID> {

    /**
     * Liefert alle Cases sortiert nach createdAt absteigend (neueste zuerst).
     */
    List<CaseEntity> findAllByOrderByCreatedAtDesc();

    /**
     * Filtert nach Status und sortiert nach createdAt absteigend.
     *
     * @param status Status als String (Enum.name() aus der Domain)
     */
    List<CaseEntity> findByStatusOrderByCreatedAtDesc(String status);

    /**
     * Filtert nach Priorität und sortiert nach createdAt absteigend.
     *
     * @param priority Priority als String (Enum.name() aus der Domain)
     */
    List<CaseEntity> findByPriorityOrderByCreatedAtDesc(String priority);

    /**
     * Kombinierter Filter: Status + Priorität, sortiert nach createdAt absteigend.
     *
     * @param status   Status als String (Enum.name())
     * @param priority Priority als String (Enum.name())
     */
    List<CaseEntity> findByStatusAndPriorityOrderByCreatedAtDesc(String status, String priority);
}
