package com.example.caseservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Smoke-Test für den Spring Application Context.
 *
 * Zweck:
 * - Prüft, ob der Spring Context erfolgreich startet
 * - Nutzt eine echte PostgreSQL-Datenbank über Testcontainers
 * - Stellt sicher, dass Konfiguration, Flyway und Beans korrekt zusammenspielen
 *
 * Abgrenzung:
 * - Kein Web-Server (webEnvironment = NONE)
 * - Keine fachlichen Assertions
 * - Fokus liegt ausschließlich auf "Application bootet fehlerfrei"
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
@ActiveProfiles("test")
class SmokeTest {

    /**
     * PostgreSQL-Testcontainer für Smoke-Tests.
     *
     * Hinweise:
     * - Eigene DB-Instanz pro Testlauf
     * - Keine Abhängigkeit von lokaler PostgreSQL-Installation
     * - Image-Version explizit gesetzt für reproduzierbare Builds
     */
    @SuppressWarnings("resource")
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("cases")
            .withUsername("postgres")
            .withPassword("postgres");

    /**
     * Überschreibt zur Laufzeit die relevanten Datasource- und Flyway-Properties.
     *
     * Ziel:
     * - Application nutzt exakt dieselbe DB für JPA und Flyway
     * - Vermeidet Konfigurationsabweichungen zwischen Test und Produktion
     */
    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        // Flyway explizit auf dieselbe Datenbank zeigen lassen
        registry.add("spring.flyway.url", postgres::getJdbcUrl);
        registry.add("spring.flyway.user", postgres::getUsername);
        registry.add("spring.flyway.password", postgres::getPassword);
    }

    /**
     * Smoke-Test:
     * - Test besteht, wenn der Spring Context ohne Exception startet
     *
     * Implizit geprüft:
     * - Datasource ist erreichbar
     * - Flyway-Migrationen laufen korrekt
     * - Alle Beans können erstellt werden
     */
    @Test
    void contextLoads() {
        // Kein Assert notwendig:
        // Ein Fehler beim Context-Start lässt den Test automatisch fehlschlagen
    }
}
