package com.example.caseservice.infrastructure.web;

import com.example.caseservice.application.dto.CaseResponse;
import com.example.caseservice.application.dto.CreateCaseRequest;
import com.example.caseservice.application.dto.UpdateCaseStatusRequest;
import com.example.caseservice.application.usecase.CreateCaseUseCase;
import com.example.caseservice.application.usecase.GetCaseUseCase;
import com.example.caseservice.application.usecase.ListCasesUseCase;
import com.example.caseservice.application.usecase.UpdateCaseStatusUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * REST-Controller für Case-Endpunkte.
 *
 * Responsibilities:
 * - Definiert REST-konforme API-Endpunkte (HTTP-Verben, Pfade, Statuscodes)
 * - Delegiert Businesslogik an UseCases (Application Layer)
 * - Führt Request-Validierung via Bean Validation (@Valid) aus
 *
 * Hinweis:
 * - Fehlerbehandlung (400/404/409) erfolgt typischerweise zentral über Exception-Handling
 *   (z. B. @ControllerAdvice), damit der Controller schlank bleibt.
 * - OpenAPI/Swagger-Annotations dienen der automatischen API-Dokumentation.
 */
@RestController
@RequestMapping("/cases")
@Tag(name = "Cases", description = "Case Management API")
public class CaseController {

    // UseCases werden per DI eingebunden (Controller enthält keine Businesslogik)
    private final CreateCaseUseCase createCaseUseCase;
    private final GetCaseUseCase getCaseUseCase;
    private final ListCasesUseCase listCasesUseCase;
    private final UpdateCaseStatusUseCase updateCaseStatusUseCase;

    public CaseController(
            CreateCaseUseCase createCaseUseCase,
            GetCaseUseCase getCaseUseCase,
            ListCasesUseCase listCasesUseCase,
            UpdateCaseStatusUseCase updateCaseStatusUseCase
    ) {
        this.createCaseUseCase = createCaseUseCase;
        this.getCaseUseCase = getCaseUseCase;
        this.listCasesUseCase = listCasesUseCase;
        this.updateCaseStatusUseCase = updateCaseStatusUseCase;
    }

    @Operation(
            summary = "Create a new case",
            description = "Creates a new case and returns the created case. The Location header points to /cases/{id}."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Created",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed",
                    content = @Content(mediaType = "application/problem+json")
            )
    })
    @PostMapping
    public ResponseEntity<CaseResponse> create(@Valid @RequestBody CreateCaseRequest request) {
        // Delegation an den Application Layer
        CaseResponse created = createCaseUseCase.execute(request);

        // REST-Konvention: Location Header zeigt auf die neue Ressource
        URI location = URI.create("/cases/" + created.id());

        return ResponseEntity.created(location).body(created);
    }

    @Operation(
            summary = "Get a case by id",
            description = "Returns a single case by its id."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Case not found",
                    content = @Content(mediaType = "application/problem+json")
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<CaseResponse> getById(
            // UUID wird automatisch von Spring aus dem Path geparst
            @Parameter(description = "Case id (UUID)", required = true)
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(getCaseUseCase.execute(id));
    }

    @Operation(
            summary = "List cases",
            description = "Lists cases. Optional filters: status, priority."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(mediaType = "application/json")
            )
    })
    @GetMapping
    public ResponseEntity<List<CaseResponse>> list(
            // Filter sind optional und werden als Optional<String> modelliert (kein null-Handling nötig)
            @Parameter(description = "Optional case status filter (e.g. OPEN, IN_PROGRESS, DONE, CLOSED)")
            @RequestParam Optional<String> status,
            @Parameter(description = "Optional priority filter (e.g. LOW, MEDIUM, HIGH, CRITICAL)")
            @RequestParam Optional<String> priority
    ) {
        return ResponseEntity.ok(listCasesUseCase.execute(status, priority));
    }

    @Operation(
            summary = "Update case status",
            description = "Updates the status of an existing case. Transitions are validated by domain rules."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CaseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid status value",
                    content = @Content(mediaType = "application/problem+json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Case not found",
                    content = @Content(mediaType = "application/problem+json")
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Invalid status transition",
                    content = @Content(mediaType = "application/problem+json")
            )
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<CaseResponse> updateStatus(
            @Parameter(description = "Case id (UUID)", required = true)
            @PathVariable UUID id,
            // Request enthält den Zielstatus als String; Validierung prüft nur "nicht leer"
            @Valid @RequestBody UpdateCaseStatusRequest request
    ) {
        // Domain-Regeln für erlaubte Transitions werden im UseCase/Domain geprüft
        return ResponseEntity.ok(updateCaseStatusUseCase.execute(id, request));
    }
}
