package com.example.caseservice.application.usecase;

import com.example.caseservice.application.dto.CaseResponse;
import com.example.caseservice.application.dto.UpdateCaseStatusRequest;
import com.example.caseservice.domain.exception.CaseNotFoundException;
import com.example.caseservice.domain.exception.InvalidCaseStatusException;
import com.example.caseservice.domain.exception.InvalidCaseStatusTransitionException;
import com.example.caseservice.domain.model.Case;
import com.example.caseservice.domain.model.CaseStatus;
import com.example.caseservice.domain.model.Priority;
import com.example.caseservice.domain.repo.CaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit-Tests für {@link UpdateCaseStatusUseCase}.
 *
 * Ziel:
 * - Deckt die wichtigsten Pfade ab:
 *   - 404 (Case existiert nicht)
 *   - 400 (Status leer/ungültig)
 *   - 409 (Status-Transition nicht erlaubt)
 *   - Happy Path (gültiger Übergang + persistieren)
 *
 * Hinweis:
 * - Domain-Regeln werden über Case.changeStatusTo(...) geprüft
 * - Repository ist gemockt, damit der Test rein fachlich bleibt
 */
@ExtendWith(MockitoExtension.class)
class UpdateCaseStatusUseCaseTest {

    @Mock
    private CaseRepository caseRepository;

    private UpdateCaseStatusUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateCaseStatusUseCase(caseRepository);
    }

    /**
     * Not-Found Path:
     * - Repository liefert kein Ergebnis
     * - UseCase soll CaseNotFoundException werfen
     * - Es darf kein Save passieren
     */
    @Test
    void shouldThrowCaseNotFound_whenCaseDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(caseRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(CaseNotFoundException.class, () ->
                useCase.execute(id, new UpdateCaseStatusRequest("IN_PROGRESS"))
        );

        verify(caseRepository, never()).save(any());
    }

    /**
     * Bad-Request Path:
     * - Status ist leer/blank
     * - UseCase soll InvalidCaseStatusException werfen
     * - Es darf kein Save passieren
     */
    @Test
    void shouldThrowInvalidCaseStatus_whenStatusIsBlank() {
        UUID id = UUID.randomUUID();
        Case existing = sampleCase(id, CaseStatus.OPEN);

        when(caseRepository.findById(id)).thenReturn(Optional.of(existing));

        assertThrows(InvalidCaseStatusException.class, () ->
                useCase.execute(id, new UpdateCaseStatusRequest("   "))
        );

        verify(caseRepository, never()).save(any());
    }

    /**
     * Bad-Request Path:
     * - Status ist kein gültiger Enum-Wert
     * - UseCase soll InvalidCaseStatusException werfen
     * - Es darf kein Save passieren
     */
    @Test
    void shouldThrowInvalidCaseStatus_whenStatusIsUnknownEnumValue() {
        UUID id = UUID.randomUUID();
        Case existing = sampleCase(id, CaseStatus.OPEN);

        when(caseRepository.findById(id)).thenReturn(Optional.of(existing));

        assertThrows(InvalidCaseStatusException.class, () ->
                useCase.execute(id, new UpdateCaseStatusRequest("NOT_A_STATUS"))
        );

        verify(caseRepository, never()).save(any());
    }

    /**
     * Conflict Path:
     * - Statuswert ist syntaktisch gültig
     * - Transition ist fachlich nicht erlaubt
     * - Domain wirft InvalidCaseStatusTransitionException
     */
    @Test
    void shouldThrowInvalidTransition_whenTransitionNotAllowed() {
        UUID id = UUID.randomUUID();
        Case existing = sampleCase(id, CaseStatus.CLOSED);

        when(caseRepository.findById(id)).thenReturn(Optional.of(existing));

        assertThrows(InvalidCaseStatusTransitionException.class, () ->
                useCase.execute(id, new UpdateCaseStatusRequest("OPEN"))
        );

        verify(caseRepository, never()).save(any());
    }

    /**
     * Happy Path:
     * - Status wird normalisiert (z. B. "in_progress")
     * - Transition OPEN -> IN_PROGRESS ist erlaubt
     * - Case wird gespeichert und als DTO zurückgegeben
     */
    @Test
    void shouldUpdateStatus_whenTransitionIsValid() {
        UUID id = UUID.randomUUID();
        Case existing = sampleCase(id, CaseStatus.OPEN);

        when(caseRepository.findById(id)).thenReturn(Optional.of(existing));

        // Repository gibt das gespeicherte Objekt unverändert zurück (für Unit-Test ausreichend)
        when(caseRepository.save(any(Case.class))).thenAnswer(inv -> inv.getArgument(0));

        CaseResponse response = useCase.execute(id, new UpdateCaseStatusRequest("in_progress"));

        // --- Assertions: Response ---
        assertEquals(id, response.id());
        assertEquals(CaseStatus.IN_PROGRESS, response.status());
        assertEquals("Case title", response.title());
        assertEquals(Priority.HIGH, response.priority());
        assertNotNull(response.updatedAt());

        // --- Verifikation: gespeicherter Domain-Case hat den neuen Status ---
        ArgumentCaptor<Case> captor = ArgumentCaptor.forClass(Case.class);
        verify(caseRepository).save(captor.capture());

        Case saved = captor.getValue();
        assertEquals(id, saved.getId());
        assertEquals(CaseStatus.IN_PROGRESS, saved.getStatus());
    }

    /**
     * Test-Helper für konsistente Domain-Objekte.
     * createdAt/updatedAt sind fix, um Tests stabil zu halten.
     */
    private static Case sampleCase(UUID id, CaseStatus status) {
        Instant created = Instant.parse("2026-01-01T10:00:00Z");
        Instant updated = Instant.parse("2026-01-01T10:00:00Z");

        return new Case(
                id,
                "Case title",
                "Case description",
                status,
                Priority.HIGH,
                null,
                created,
                updated
        );
    }
}
