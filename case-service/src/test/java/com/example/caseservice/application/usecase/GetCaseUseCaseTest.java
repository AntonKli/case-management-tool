package com.example.caseservice.application.usecase;

import com.example.caseservice.application.dto.CaseResponse;
import com.example.caseservice.domain.exception.CaseNotFoundException;
import com.example.caseservice.domain.model.Case;
import com.example.caseservice.domain.model.CaseStatus;
import com.example.caseservice.domain.model.Priority;
import com.example.caseservice.domain.repo.CaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit-Tests für {@link GetCaseUseCase}.
 *
 * Ziel:
 * - Testet das Verhalten des UseCases isoliert (ohne Spring Context)
 * - Prüft Mapping Domain -> DTO sowie den Not-Found-Fall
 */
@ExtendWith(MockitoExtension.class)
class GetCaseUseCaseTest {

    // Repository wird gemockt, um den UseCase unabhängig von DB/Persistenz zu testen
    @Mock
    private CaseRepository caseRepository;

    // Zu testender UseCase
    private GetCaseUseCase useCase;

    @BeforeEach
    void setUp() {
        // UseCase mit Mock-Repository initialisieren
        useCase = new GetCaseUseCase(caseRepository);
    }

    /**
     * Happy Path:
     * - Case existiert im Repository
     * - UseCase mappt korrekt auf CaseResponse
     */
    @Test
    void shouldReturnCaseResponse_whenCaseExists() {
        UUID id = UUID.randomUUID();

        // Testdaten als Domain-Entity (repräsentiert den DB-Zustand)
        Case existing = new Case(
                id,
                "Case title",
                "Case description",
                CaseStatus.OPEN,
                Priority.HIGH,
                null,
                Instant.parse("2026-01-01T10:00:00Z"),
                Instant.parse("2026-01-02T10:00:00Z")
        );

        when(caseRepository.findById(id)).thenReturn(Optional.of(existing));

        CaseResponse response = useCase.execute(id);

        // --- Assertions: DTO entspricht der Domain-Entity ---
        assertNotNull(response);
        assertEquals(existing.getId(), response.id());
        assertEquals(existing.getTitle(), response.title());
        assertEquals(existing.getDescription(), response.description());
        assertEquals(existing.getStatus(), response.status());
        assertEquals(existing.getPriority(), response.priority());
        assertEquals(existing.getAssigneeId(), response.assigneeId());
        assertEquals(existing.getCreatedAt(), response.createdAt());
        assertEquals(existing.getUpdatedAt(), response.updatedAt());

        // Repository soll genau einmal genutzt werden, sonst nichts
        verify(caseRepository).findById(id);
        verifyNoMoreInteractions(caseRepository);
    }

    /**
     * Not-Found Path:
     * - Repository liefert kein Ergebnis
     * - UseCase wirft eine fachliche CaseNotFoundException
     *   (wird später im REST-Layer zu HTTP 404 gemappt)
     */
    @Test
    void shouldThrowCaseNotFound_whenCaseDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(caseRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(CaseNotFoundException.class, () -> useCase.execute(id));

        verify(caseRepository).findById(id);
        verifyNoMoreInteractions(caseRepository);
    }
}
