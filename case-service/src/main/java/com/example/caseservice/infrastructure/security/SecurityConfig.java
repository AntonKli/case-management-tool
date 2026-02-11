package com.example.caseservice.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security-Konfiguration für den Case-Service.
 *
 * Zweck:
 * - Definiert eine bewusst minimale Security-Konfiguration
 * - Ermöglicht den Betrieb als stateless REST-API
 *
 * Hinweis:
 * - Diese Konfiguration ist für Demo-/Referenzzwecke gedacht
 * - Authentifizierung/Autorisierung kann später ergänzt werden
 *   (z. B. JWT, OAuth2, Rollen)
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Zentrale Spring-Security-Konfiguration.
     *
     * Entscheidungen:
     * - CSRF deaktiviert (typisch für stateless REST-APIs)
     * - Keine HTTP-Sessions (STATELESS)
     * - Alle Requests erlaubt (kein Auth-Zwang)
     * - HTTP Basic und Form-Login deaktiviert
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // CSRF ist für klassische Browser-Form-Logins relevant,
                // nicht für eine stateless JSON-API
                .csrf(csrf -> csrf.disable())

                // Keine serverseitigen Sessions (REST-Prinzip)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Alle Requests sind aktuell erlaubt
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())

                // Deaktivieren von Standard-Auth-Mechanismen
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())

                .build();
    }
}
