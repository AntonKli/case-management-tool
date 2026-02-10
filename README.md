# Case Management Tool

> Full-Stack Case-Management-Anwendung mit React und Spring Boot.
> Fokus auf Clean Architecture, explizite Businessregeln und realistische Backend-Validierung
> inkl. Flyway, Testcontainers und klarer Fehlersemantik.

---

## Projektüberblick

Dieses Projekt ist ein vollständiges Case-Management-Tool mit dem Ziel, eine realistische, saubere und professionell strukturierte Anwendung zu demonstrieren.
Der Fokus liegt bewusst nicht auf einer isolierten Testumgebung, sondern auf einem durchdachten Gesamtsystem,
das sich in ähnlicher Form auch in realen Projekten einsetzen lässt.

---

## Ziele des Projekts

* Saubere, nachvollziehbare Architektur
* Klare Trennung von Verantwortlichkeiten (UI, Application, Domain, Persistence)
* Verständlicher, kommentierter Code
* Explizite Businessregeln (z. B. Status-Transitions)
* Stabiler technischer Unterbau (Tests, Validierung, Fehlerbehandlung)

---

## Technologie-Stack

* **Frontend:** React, Vite, React Router
* **Backend:** Java 17, Spring Boot, Maven
* **Datenbank:** PostgreSQL (Docker)
* **Migrationen:** Flyway
* **Tests:** JUnit 5, Mockito, Testcontainers

---

## Architekturüberblick

Das Projekt folgt einer an Clean Architecture angelehnten Schichtenarchitektur.
Die Businesslogik ist strikt von UI und Infrastruktur getrennt.

**Architekturfluss:**

Frontend (React)
→ API-Schicht (casesApi.js)
→ Backend REST API (Controller)
→ Application Layer (UseCases)
→ Domain Layer (Businessregeln & Modelle)
→ Persistence Layer (Repository)
→ PostgreSQL

---

## Frontend (case-ui)

**Technologien:**

* React + Vite
* React Router
* Zentrale API-Abstraktion mit einheitlichem Fehlerhandling

**Eigenschaften:**

* Klare Trennung von Darstellung und Logik
* Visuelle Statusdarstellung (OPEN, IN_PROGRESS, DONE, CLOSED)
* Sauberes Fehler- und Ladezustandsmanagement
* Bewusst simples, funktionales UI mit Fokus auf Verständlichkeit

---

## Backend (case-service)

**Technologien:**

* Java 17
* Spring Boot
* Maven
* PostgreSQL

**Konzepte:**

* UseCase-orientierte Application-Schicht
* Domain-getriebene Statuslogik mit expliziten Transition-Regeln
* Repository-Abstraktion
* REST-konforme Endpunkte

---

## Validierung & Businessregeln

Die Validierung erfolgt **ausschließlich im Backend**.
Das Frontend stellt lediglich verständliche Fehlermeldungen dar.

* Ungültige Statuswerte → **400 Bad Request**
* Ungültige Status-Übergänge → **409 Conflict**
* Nicht existierende Ressourcen → **404 Not Found**

**Erlaubte Status-Transition:**

OPEN → IN_PROGRESS → DONE → CLOSED

---

## Fehlerbehandlung

* Einheitliche Fehlerantworten über **RFC 7807 (ProblemDetail)**
* Klare HTTP-Semantik (400 / 404 / 409 / 500)
* Separate Behandlung für ungültige API-Pfade unter `/api`

---

## REST API Endpunkte (Beispiele)

* `GET    /cases`
* `GET    /cases/{id}`
* `POST   /cases`
* `PATCH  /cases/{id}/status`

Standard-Port: `http://localhost:8082`

---

## Tests & Qualitätssicherung

* Unit-Tests für UseCases (JUnit 5, Mockito)
* Integrationstests mit echter PostgreSQL-Datenbank (Testcontainers)
* Smoke-Tests zum Verifizieren von Flyway-Migrationen, JPA-Mapping und Context-Startup

---

## Projektstruktur

```
case_management_tool/
├─ case-service/   # Backend (Spring Boot)
├─ case-ui/        # Frontend (React + Vite)
├─ docs/
│  └─ screenshots/ # UI Screenshots für README
└─ README.md
```

---

## Screenshots

Die UI wird im README anhand von Screenshots dokumentiert:

* details.png
* erstelle.png
* uebersicht.png

---

## Projekt starten

### Backend

```bash
cd case-service
mvn -DskipTests spring-boot:run
```

### Frontend

```bash
cd case-ui
npm install
npm run dev
```

### PostgreSQL (Docker)

```bash
docker ps
docker start case-service-postgres
```

---

## Security-Hinweis

Die Anwendung erlaubt aktuell alle Requests (Demo-Setup).
Security-Abhängigkeiten sind bewusst vorbereitet, um einen späteren Ausbau
(z. B. JWT, Rollen, OAuth2) zu ermöglichen.

---

## Warum dieses Projekt?

Dieses Projekt dient als Referenz für saubere Softwarearchitektur.
Es zeigt, dass neben reiner Funktionalität insbesondere Struktur, Wartbarkeit,
klare Verantwortlichkeiten und bewusste Architekturentscheidungen entscheidend sind.

---

## Weiterer Ausbau (optional)

* Authentifizierung & Rollen
* Case-Zuweisung (Assignees)
* Filter & Suche
* Pagination
* UI-Feinschliff
