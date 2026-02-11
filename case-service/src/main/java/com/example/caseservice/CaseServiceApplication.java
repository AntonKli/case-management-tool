package com.example.caseservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Einstiegspunkt der Spring-Boot-Anwendung.
 *
 * Zweck:
 * - Startet den Case-Service
 * - Initialisiert den Spring Application Context
 * - Aktiviert Auto-Configuration, Component-Scan und Configuration Properties
 *
 * Hinweis:
 * - @SpringBootApplication ist eine Sammelannotation für:
 *   - @Configuration
 *   - @EnableAutoConfiguration
 *   - @ComponentScan
 * - Diese Klasse enthält bewusst keine weitere Logik
 */
@SpringBootApplication
public class CaseServiceApplication {

    /**
     * Main-Methode zum Starten der Anwendung.
     *
     * @param args Kommandozeilenargumente (z. B. Profile, Ports)
     */
    public static void main(String[] args) {
        SpringApplication.run(CaseServiceApplication.class, args);
    }
}
