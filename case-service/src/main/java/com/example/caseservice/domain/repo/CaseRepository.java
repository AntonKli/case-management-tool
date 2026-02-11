package com.example.caseservice.domain.repo;

import com.example.caseservice.domain.model.Case;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository-Interface für den Zugriff auf Case-Daten.
 *
 * Zweck:
 * - Definiert die fachliche Schnittstelle zur Persistenz
 * - Entkoppelt Domain/Application von konkreten Datenbank-Technologien
 *
 * Hinweise:
 * - Dieses Interface gehört bewusst zur Domain-Schicht
 * - Implementierungen (z. B. JPA, JDBC) liegen im Infrastructure-/Persistence-Layer
 */
public interface CaseRepository {

    /**
     * Persistiert einen Case.
     *
     * Hinweis:
     * - Kann sowohl für Create als auch Update verwendet werden
     * - Gibt den tatsächlich gespeicherten Zustand zurück
     *   (z. B. mit gesetzten Timestamps)
     */
    Case save(Case caze);

    /**
     * Liefert einen Case anhand seiner ID.
     *
     * @param id technische ID des Cases
     * @return Optional mit Case, oder leer wenn nicht vorhanden
     */
    Optional<Case> findById(UUID id);

    /**
     * Liefert alle Cases, optional gefiltert nach Status und/oder Priorität.
     *
     * @param status   optionaler Status-Filter (z. B. "OPEN")
     * @param priority optionale Priorität (z. B. "HIGH")
     *
     * Hinweis:
     * - Filter sind optional, um flexible Abfragen zu ermöglichen
     * - Interpretation und Mapping der Filter erfolgt in der Implementierung
     */
    List<Case> findAll(Optional<String> status, Optional<String> priority);
}
