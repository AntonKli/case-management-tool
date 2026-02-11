package com.example.caseservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Integrationstest mit echter PostgreSQL-Datenbank (via Testcontainers).
 *
 * Zweck:
 * - Stellt sicher, dass die Anwendung mit einer realen PostgreSQL-Instanz startet
 * - Verifiziert, dass Flyway-Migrationen korrekt angewendet werden
 * - Prüft, dass Hibernate-Mapping zum DB-Schema passt (ddl-auto=validate)
 *
 * Wichtig:
 * - Kein Mocking
 * - Keine In-Memory-DB
 * - Umgebung möglichst nah an Produktion
 */
@Testcontainers
@SpringBootTest
class CasePostgresIT {

    /**
     * PostgreSQL-Testcontainer.
     *
     * Hinweise:
     * - Container wird automatisch vor dem Test gestartet
     * - Nach dem Test wieder sauber gestoppt
     * - Image-Version ist explizit festgelegt (Reproduzierbarkeit)
     */
    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    /**
     * Dynamische Überschreibung von Spring Properties zur Laufzeit.
     *
     * Ziel:
     * - Spring Boot nutzt die vom Testcontainer bereitgestellte DB
     * - Keine Abhängigkeit von lokaler PostgreSQL-Installation
     */
    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);

        // Genau das Verhalten, das wir auch in einer "echten" Umgebung erwarten:
        // - Hibernate validiert nur das Schema (keine Änderungen!)
        // - Flyway ist die einzige Quelle für DB-Migrationen
        r.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        r.add("spring.flyway.enabled", () -> "true");
        r.add("spring.flyway.locations", () -> "classpath:db/migration");
    }

    /**
     * Smoke-Test:
     * - Wenn der Spring Context erfolgreich startet, ist der Test bestanden
     *
     * Implizit geprüft:
     * - PostgreSQL ist erreichbar
     * - Flyway-Migrationen laufen fehlerfrei
     * - Hibernate-Mapping passt zum Schema
     */
    @Test
    void contextLoadsWithPostgres() {
        // Kein expliziter Assert nötig:
        // Ein Fehler beim Starten des Contexts lässt den Test fehlschlagen
    }
}
