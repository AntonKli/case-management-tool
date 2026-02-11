package com.example.caseservice.domain.model;

import com.example.caseservice.domain.exception.InvalidCaseStatusTransitionException;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain-Entity für einen Case.
 *
 * Zweck:
 * - Repräsentiert den fachlichen Kern eines Cases
 * - Enthält ausschließlich Domain-Zustand und Businesslogik
 *
 * Design-Entscheidungen:
 * - Immutable Entity (keine Setter)
 * - Änderungen erzeugen neue Instanzen
 * - Businessregeln (z. B. Status-Transitions) liegen direkt in der Domain
 */
public class Case {

    // Technische Identität des Cases
    private final UUID id;

    // Fachlicher Titel (Pflichtfeld)
    private final String title;

    // Optionale Beschreibung
    private final String description;

    // Aktueller Status des Cases (Domain-Enum)
    private final CaseStatus status;

    // Priorität des Cases (Domain-Enum)
    private final Priority priority;

    // Optionaler Bearbeiter (kann null sein)
    private final UUID assigneeId;

    // Zeitstempel der Erstellung (immutable)
    private final Instant createdAt;

    // Zeitstempel der letzten Änderung
    private final Instant updatedAt;

    public Case(
            UUID id,
            String title,
            String description,
            CaseStatus status,
            Priority priority,
            UUID assigneeId,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.assigneeId = assigneeId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // --- Getter (keine Setter, um Immutability zu wahren) ---

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public CaseStatus getStatus() {
        return status;
    }

    public Priority getPriority() {
        return priority;
    }

    public UUID getAssigneeId() {
        return assigneeId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Führt einen Statuswechsel für den Case durch.
     *
     * Businessregeln:
     * - newStatus darf nicht null sein
     * - Ein Wechsel auf den gleichen Status ist ein No-Op
     * - Erlaubte Transitions werden über CaseStatus.canTransitionTo definiert
     *
     * @param newStatus gewünschter neuer Status
     * @return neue Case-Instanz mit aktualisiertem Status
     *
     * @throws InvalidCaseStatusTransitionException
     *         wenn der Statuswechsel fachlich nicht erlaubt ist
     */
    public Case changeStatusTo(CaseStatus newStatus) {
        // Defensive: Domain-Methoden dürfen nicht mit null aufgerufen werden
        if (newStatus == null) {
            throw new IllegalArgumentException("newStatus must not be null");
        }

        // No-Op: gleicher Status → keine neue Instanz nötig
        if (this.status == newStatus) {
            return this;
        }

        // Fachliche Prüfung der Transition (Domain-Regel)
        if (!this.status.canTransitionTo(newStatus)) {
            throw new InvalidCaseStatusTransitionException(
                    this.id,
                    this.status,
                    newStatus
            );
        }

        // Immutability: es wird eine neue Instanz mit aktualisiertem Status erzeugt
        return new Case(
                this.id,
                this.title,
                this.description,
                newStatus,
                this.priority,
                this.assigneeId,
                this.createdAt,
                Instant.now()
        );
    }
}
