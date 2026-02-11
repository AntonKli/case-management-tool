-- Initiale Datenbankmigration für das Case-Management-System
-- Version: V1
--
-- Zweck:
-- - Legt die grundlegende Tabellenstruktur für Cases an
-- - Spiegelt bewusst das Domain-Modell wider (Case)
-- - Wird typischerweise über Flyway/Liquibase beim Start ausgeführt

create table cases (
  -- Technische Primär-ID des Cases (UUID wird in der Application erzeugt)
  id uuid primary key,

  -- Fachlicher Titel des Cases (Pflichtfeld, begrenzte Länge)
  title varchar(200) not null,

  -- Optionale Beschreibung mit größerem Textumfang
  description varchar(4000),

  -- Aktueller Status des Cases (Domain-Enum als String)
  status varchar(30) not null,

  -- Priorität des Cases (Domain-Enum als String)
  priority varchar(30) not null,

  -- Optionaler Bearbeiter (für spätere Erweiterungen, z. B. Zuweisungen)
  assignee_id uuid null,

  -- Zeitpunkt der Erstellung (serverseitig gesetzt)
  created_at timestamp not null,

  -- Zeitpunkt der letzten Änderung (z. B. Status-Update)
  updated_at timestamp not null
);
