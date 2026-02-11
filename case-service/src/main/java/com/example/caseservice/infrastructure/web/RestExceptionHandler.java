package com.example.caseservice.infrastructure.web;

import com.example.caseservice.domain.exception.CaseNotFoundException;
import com.example.caseservice.domain.exception.InvalidCaseStatusException;
import com.example.caseservice.domain.exception.InvalidCaseStatusTransitionException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

/**
 * Zentraler Exception-Handler für die REST-API.
 *
 * Responsibilities:
 * - Übersetzt Exceptions in konsistente HTTP-Responses
 * - Verwendet ProblemDetail (RFC 7807) als standardisiertes Fehlerformat
 * - Stellt sicher, dass typische Fehlerfälle sauber gemappt werden:
 *   - 400 Bad Request (Validierung / ungültiger Input)
 *   - 404 Not Found (Ressource existiert nicht)
 *   - 409 Conflict (Businessregel / Status-Transition)
 *   - 500 Internal Server Error (Fallback)
 *
 * Hinweis:
 * - Controllers bleiben dadurch schlank (kein try/catch pro Endpoint)
 * - Das Frontend kann sich auf HTTP-Status + strukturierte Fehlermeldung verlassen
 */
@RestControllerAdvice
public class RestExceptionHandler {

    /**
     * 400: Request-Body Validierung fehlgeschlagen (@Valid auf DTOs).
     *
     * Beispiel:
     * - title ist leer (NotBlank)
     * - title zu lang (Size)
     *
     * Wir geben zusätzlich ein Feld-Mapping zurück, damit das Frontend
     * Fehlermeldungen direkt pro Eingabefeld anzeigen kann.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Validation failed");
        pd.setDetail("Request validation failed");

        Map<String, Object> fields = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> fields.put(err.getField(), err.getDefaultMessage()));

        pd.setProperty("fields", fields);
        return pd;
    }

    /**
     * 400: Validierungsfehler außerhalb des Request-Bodys (z. B. @RequestParam, @PathVariable).
     *
     * Beispiel:
     * - Constraint-Verletzungen bei Parametern
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Validation failed");
        pd.setDetail(ex.getMessage());
        return pd;
    }

    /**
     * 404: Case existiert nicht (fachlicher Not-Found-Fall).
     */
    @ExceptionHandler(CaseNotFoundException.class)
    public ProblemDetail handleCaseNotFound(CaseNotFoundException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        pd.setTitle("Not found");
        pd.setDetail(ex.getMessage());
        return pd;
    }

    /**
     * 400: Ungültiger Statuswert (Enum-Parsing/Value).
     *
     * Hinweis:
     * - Aktuell existieren zwei InvalidCaseStatusException-Klassen (Domain + Application).
     * - Beide werden hier auf 400 gemappt, damit das Verhalten konsistent bleibt.
     * - Ideal wäre langfristig nur eine Klasse (vorzugsweise Domain), um Duplikate zu vermeiden.
     */
    @ExceptionHandler({
            InvalidCaseStatusException.class,
            com.example.caseservice.application.usecase.InvalidCaseStatusException.class
    })
    public ProblemDetail handleInvalidStatus(RuntimeException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Invalid status");
        pd.setDetail(ex.getMessage());
        return pd;
    }

    /**
     * 409: Status-Transition fachlich nicht erlaubt (Businessregel verletzt).
     *
     * Beispiel:
     * - OPEN → DONE (überspringt IN_PROGRESS)
     * - CLOSED → IN_PROGRESS (Rücksprung nicht erlaubt)
     */
    @ExceptionHandler(InvalidCaseStatusTransitionException.class)
    public ProblemDetail handleInvalidTransition(InvalidCaseStatusTransitionException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        pd.setTitle("Invalid status transition");
        pd.setDetail(ex.getMessage());
        return pd;
    }

    /**
     * 404: Kein Endpoint gemappt.
     *
     * Hintergrund:
     * - Ohne Handler kann Spring (je nach Setup) auf Static-Resource-Handling fallen,
     *   was für API-Clients unklare Fehlermeldungen erzeugen kann.
     * - Wir liefern bewusst eine neutrale 404-Antwort.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail handleNoResourceFound(NoResourceFoundException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        pd.setTitle("Not found");
        pd.setDetail("No endpoint found for this path.");
        return pd;
    }

    /**
     * 400: Typische Bad-Request-Fälle durch defensive Checks.
     *
     * Beispiele:
     * - Argumente fehlen (null/blank)
     * - Enum.valueOf schlägt fehl und wird als IllegalArgumentException geworfen
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Bad request");
        pd.setDetail(ex.getMessage());
        return pd;
    }

    /**
     * 500: Fallback-Handler für unerwartete Fehler.
     *
     * Hinweis:
     * - Sollte nach den spezifischen Handlern nur noch echte Bugs / Infrastrukturprobleme abfangen
     * - Detail-Message wird absichtlich nicht direkt an den Client gegeben (Security/Noise)
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        pd.setTitle("Internal error");
        pd.setDetail("Unexpected error");
        return pd;
    }
}
