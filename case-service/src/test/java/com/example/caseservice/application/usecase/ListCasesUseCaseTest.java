package com.example.caseservice.application.usecase;

import com.example.caseservice.application.dto.CaseResponse;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit-Tests für {@link ListCasesUseCase}.
 *
 * Ziel:
 * - Stellt sicher, dass Filterparameter korrekt an das Repository weitergegeben werden
 * - Prüft das Mapping Domain -> CaseResponse
 * - Testet Verhalten sowohl mit als auch ohne Filter
 */
@ExtendWith(MockitoExtension.class)
class ListCasesUseCaseTest {

    // Repository wird gemockt, um den UseCase unabhängig von Persistenz zu testen
    @Mock
    private CaseRepository caseRepository;

    // Zu testender UseCase
    private ListCasesUseCase useCase;

    @BeforeEach
    void setUp() {
        // UseCase mit Mock-Repository initialisieren
        useCase = new ListCasesUseCase(caseRepository);
    }

    /**
     * Happy Path mit gesetzten Filtern:
     * - UseCase soll Filter unverändert ans Repository durchreichen
     * - Ergebnisse sollen korrekt auf CaseResponse gemappt werden
     */
    @Test
    void shouldForwardFiltersToRepository_andMapToCaseResponseList() {
        Optional<String> status = Optional.of("OPEN");
        Optional<String> priority = Optional.of("HIGH");

        // Testdaten: zwei Cases mit gleichem Filter, aber unterschiedlicher Description
        Case c1 = sampleCase(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "Case 1",
                "Desc 1",
                CaseStatus.OPEN,
                Priority.HIGH
        );

        Case c2 = sampleCase(
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                "Case 2",
                null,
                CaseStatus.OPEN,
                Priority.HIGH
        );

        when(caseRepository.findAll(status, priority)).thenReturn(List.of(c1, c2));

        List<CaseResponse> result = useCase.execute(status, priority);

        // --- Assertions: Ergebnisliste ---
        assertNotNull(result);
        assertEquals(2, result.size());

        // --- Assertions: Mapping des ersten Elements ---
        CaseResponse r1 = result.get(0);
        assertEquals(c1.getId(), r1.id());
        assertEquals(c1.getTitle(), r1.title());
        assertEquals(c1.getDescription(), r1.description());
        assertEquals(c1.getStatus(), r1.status());
        assertEquals(c1.getPriority(), r1.priority());
        assertEquals(c1.getAssigneeId(), r1.assigneeId());
        assertEquals(c1.getCreatedAt(), r1.createdAt());
        assertEquals(c1.getUpdatedAt(), r1.updatedAt());

        // --- Assertions: Mapping des zweiten Elements (description kann null sein) ---
        CaseResponse r2 = result.get(1);
        assertEquals(c2.getId(), r2.id());
        assertEquals(c2.getTitle(), r2.title());
        assertNull(r2.description());
        assertEquals(c2.getStatus(), r2.status());
        assertEquals(c2.getPriority(), r2.priority());

        // --- Verifikation: Filter wurden unverändert übergeben ---
        ArgumentCaptor<Optional<String>> statusCaptor = ArgumentCaptor.captor();
        ArgumentCaptor<Optional<String>> priorityCaptor = ArgumentCaptor.captor();

        verify(caseRepository).findAll(statusCaptor.capture(), priorityCaptor.capture());
        assertEquals(status, statusCaptor.getValue());
        assertEquals(priority, priorityCaptor.getValue());

        // Keine weiteren Repository-Calls erwartet
        verifyNoMoreInteractions(caseRepository);
    }

    /**
     * Verhalten ohne Filter:
     * - Optional.empty() soll unterstützt werden
     * - Rückgabe kann eine leere Liste sein
     */
    @Test
    void shouldWorkWithEmptyFilters() {
        Optional<String> status = Optional.empty();
        Optional<String> priority = Optional.empty();

        when(caseRepository.findAll(status, priority)).thenReturn(List.of());

        List<CaseResponse> result = useCase.execute(status, priority);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(caseRepository).findAll(status, priority);
        verifyNoMoreInteractions(caseRepository);
    }

    /**
     * Test-Helper zum Erzeugen konsistenter Domain-Objekte.
     * - createdAt/updatedAt sind fix, um Tests stabil zu halten
     */
    private static Case sampleCase(UUID id, String title, String description, CaseStatus status, Priority priority) {
        Instant created = Instant.parse("2026-01-01T10:00:00Z");
        Instant updated = Instant.parse("2026-01-01T10:00:00Z");

        return new Case(
                id,
                title,
                description,
                status,
                priority,
                null,
                created,
                updated
        );
    }
}
