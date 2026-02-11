package com.example.caseservice.domain.model;

/**
 * Domain-Enum für die Priorität eines Cases.
 *
 * Zweck:
 * - Definiert alle fachlich gültigen Prioritätsstufen
 * - Wird sowohl im Domain- als auch im Application-Layer verwendet
 *
 * Hinweise:
 * - Die Reihenfolge hat keine technische Bedeutung,
 *   sondern dient ausschließlich der fachlichen Einordnung
 * - Erweiterungen (z. B. VERY_HIGH) können zentral hier erfolgen
 */
public enum Priority {

    // Geringe Priorität (kein Zeitdruck)
    LOW,

    // Normale Priorität (Standardfall)
    MEDIUM,

    // Hohe Priorität (zeitnah bearbeiten)
    HIGH,

    // Kritische Priorität (sofortige Aufmerksamkeit erforderlich)
    CRITICAL
}
