package com.example.caseservice.infrastructure.persistence;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA-Entity für die Persistenz eines Cases.
 *
 * Zweck:
 * - Repräsentiert die Datenbank-Struktur eines Cases
 * - Dient ausschließlich der Speicherung/Ladung aus der DB
 *
 * Hinweise:
 * - Diese Klasse gehört bewusst zum Infrastructure-/Persistence-Layer
 * - Sie ist vom Domain-Modell (Case) getrennt
 * - Mapping zwischen Entity und Domain erfolgt außerhalb (z. B. im Repository)
 */
@Entity
@Table(name = "cases")
public class CaseEntity {

    // Primärschlüssel (wird vom Application-Layer erzeugt)
    @Id
    private UUID id;

    // Fachlicher Titel (Pflichtfeld)
    @Column(nullable = false, length = 200)
    private String title;

    // Optionale Beschreibung
    @Column(length = 4000)
    private String description;

    // Status wird als String gespeichert (Enum-Mapping erfolgt im Domain/Application-Layer)
    @Column(nullable = false, length = 30)
    private String status;

    // Priorität wird ebenfalls als String persistiert
    @Column(nullable = false, length = 30)
    private String priority;

    // Optionaler Bearbeiter (Foreign-Key-ähnlich, aber bewusst ohne Relation)
    @Column(name = "assignee_id")
    private UUID assigneeId;

    // Erstellungszeitpunkt (serverseitig gesetzt)
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    // Letzter Änderungszeitpunkt (z. B. Status-Update)
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Geschützter No-Args-Konstruktor für JPA.
     * Wird vom Framework benötigt und nicht direkt verwendet.
     */
    protected CaseEntity() {}

    /**
     * Vollständiger Konstruktor für explizites Mapping
     * von Domain-Objekten zu Persistenz-Entities.
     */
    public CaseEntity(
            UUID id,
            String title,
            String description,
            String status,
            String priority,
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

    // --- Getter (keine Setter, Entity wird kontrolliert über Mapping erzeugt) ---

    public UUID getId() { return id; }

    public String getTitle() { return title; }

    public String getDescription() { return description; }

    public String getStatus() { return status; }

    public String getPriority() { return priority; }

    public UUID getAssigneeId() { return assigneeId; }

    public Instant getCreatedAt() { return createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
}
