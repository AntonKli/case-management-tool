package com.example.caseservice.application.usecase;

import com.example.caseservice.application.dto.CaseResponse;
import com.example.caseservice.application.dto.CreateCaseRequest;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit-Tests für {@link CreateCaseUseCase}.
 *
 * Ziel:
 * - Testet die Businesslogik isoliert (ohne Spring Context)
 * - Verifiziert Validierung, Default-Werte und Mapping
 *
 * Hinweis:
 * - Das Repository wird gemockt, damit keine echte Persistenz involviert ist
 * - Wir testen ausschließlich das Verhalten des UseCases
 */
@ExtendWith(MockitoExtension.class)
class CreateCaseUseCaseTest {

    // Mock des Repository-Interfaces (Domain-Abhängigkeit)
    @Mock
    private CaseRepository caseRepository;

    // Zu testender UseCase
    private CreateCaseUseCase useCase;

    @BeforeEach
    void setUp() {
        // UseCase wird manuell mit Mock-Repository erstellt
        useCase = new CreateCaseUseCase(caseRepository);
    }

    /**
     * Defensive Programmierung:
     * - Ein null-Request ist ein Programmierfehler
     * - In diesem Fall darf keine Persistenz stattfinden
     */
    @Test
    void shouldThrowIllegalArgumentException_whenRequestIsNull() {
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(null));

        // Repository darf in diesem Fall nicht aufgerufen werden
        verify(caseRepository, never()).save(any());
    }

    /**
     * Titel ist ein Pflichtfeld.
     * - Leere oder blanke Titel sind fachlich nicht erlaubt
     * - Repository darf nicht aufgerufen werden
     */
    @Test
    void shouldThrowIllegalArgumentException_whenTitleIsBlank() {
        CreateCaseRequest request = new CreateCaseRequest(
                "   ",
                "desc",
                Priority.HIGH
        );

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(request));
        verify(caseRepository, never()).save(any());
    }

    /**
     * Happy Path:
     * - Titel wird getrimmt
     * - Status wird initial auf OPEN gesetzt
     * - Priority wird übernommen
     * - Timestamps werden gesetzt
     */
    @Test
    void shouldCreateCase_withTrimmedTitle_andOpenStatus() {
        // Repository gibt das gespeicherte Objekt unverändert zurück
        // (typisches Verhalten bei einfachen Unit-Tests)
        when(caseRepository.save(any(Case.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        CreateCaseRequest request = new CreateCaseRequest(
                "   My Title   ",
                "   Some description   ", // Description wird bewusst NICHT getrimmt
                Priority.HIGH
        );

        CaseResponse response = useCase.execute(request);

        // --- Assertions auf Response-DTO ---
        assertNotNull(response);
        assertNotNull(response.id());
        assertEquals("My Title", response.title());
        assertEquals("   Some description   ", response.description());
        assertEquals(CaseStatus.OPEN, response.status());
        assertEquals(Priority.HIGH, response.priority());
        assertNotNull(response.createdAt());
        assertNotNull(response.updatedAt());

        // --- Verifikation des gespeicherten Domain-Objekts ---
        ArgumentCaptor<Case> captor = ArgumentCaptor.forClass(Case.class);
        verify(caseRepository).save(captor.capture());

        Case saved = captor.getValue();
        assertNotNull(saved.getId());
        assertEquals("My Title", saved.getTitle());
        assertEquals("   Some description   ", saved.getDescription());
        assertEquals(CaseStatus.OPEN, saved.getStatus());
        assertEquals(Priority.HIGH, saved.getPriority());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
    }
}
