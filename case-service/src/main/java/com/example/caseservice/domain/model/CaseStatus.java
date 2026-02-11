package com.example.caseservice.domain.model;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Domain-Enum für den Status eines Cases.
 *
 * Zweck:
 * - Definiert alle fachlich gültigen Case-Status
 * - Kapselt die erlaubten Status-Transitions zentral an einer Stelle
 *
 * Vorteil:
 * - Statusregeln sind explizit, nachvollziehbar und testbar
 * - Verhindert verstreute if/else-Logik in UseCases oder Controllern
 */
public enum CaseStatus {

    // Initialer Status eines neuen Cases
    OPEN,

    // Case wird aktiv bearbeitet
    IN_PROGRESS,

    // Bearbeitung abgeschlossen
    DONE,

    // Case final geschlossen (Endzustand)
    CLOSED;

    /**
     * Definiert erlaubte Status-Transitions.
     *
     * Reihenfolge:
     * OPEN → IN_PROGRESS → DONE → CLOSED
     *
     * CLOSED ist ein Endzustand und erlaubt keine weiteren Übergänge.
     */
    private static final Map<CaseStatus, Set<CaseStatus>> ALLOWED_TRANSITIONS = Map.of(
            OPEN, EnumSet.of(IN_PROGRESS),
            IN_PROGRESS, EnumSet.of(DONE),
            DONE, EnumSet.of(CLOSED),
            CLOSED, EnumSet.noneOf(CaseStatus.class)
    );

    /**
     * Prüft, ob ein Statuswechsel fachlich erlaubt ist.
     *
     * @param target gewünschter Zielstatus
     * @return true, wenn der Übergang erlaubt ist, sonst false
     */
    public boolean canTransitionTo(CaseStatus target) {
        // Defensive: null-Zielstatus ist niemals erlaubt
        if (target == null) {
            return false;
        }

        // Lookup in der Transition-Map
        return ALLOWED_TRANSITIONS
                .getOrDefault(this, Set.of())
                .contains(target);
    }
}
