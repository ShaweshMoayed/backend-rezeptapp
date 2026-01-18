# RezeptApp – Backend 🍽️

**Thema:** Rezept-App  
**Team:** Moayed Shawesh – Einzelarbeit  
**Modul:** Webtechnologien, HTW Berlin

---

## 📋 Projektbeschreibung

Das Backend der **RezeptApp** ist eine REST API, die mit **Spring Boot** entwickelt wurde.  
Sie stellt die serverseitige Logik für eine Webanwendung zur Verwaltung von **Rezepten**, **Essensplänen** und **Nährwertstatistiken** bereit.

Die API wird von einem **Vue.js Frontend** genutzt und speichert Daten persistent in einer **PostgreSQL-Datenbank**.

### Nutzer können:
- Rezepte erstellen, bearbeiten, löschen und favorisieren
- Essenspläne für Kalenderwochen anlegen
- Essenspläne als **PDF exportieren**
- Nährwertstatistiken berechnen
- sich registrieren und authentifizieren

---

## 🛠️ Tech Stack

> **Entsprechend der Modul-Vorgaben**

| Kategorie | Technologie |
|---------|------------|
| Sprache | **Java 25** |
| Framework | **Spring Boot 3.5.x** |
| Build Tool | **Gradle** |
| Datenbank (Prod) | **PostgreSQL** |
| Datenbank (Tests) | **H2** |
| Persistenz | Spring Data JPA |
| Validierung | Jakarta Validation |
| Sicherheit | Password Hashing (Spring Security Crypto) |
| PDF | OpenPDF |
| Tests | JUnit 5, MockMvc, SpringBootTest |
| CI/CD | GitHub Actions |
| Deployment | Render.com |
| Container | Docker |

---

## 🚀 Schnellstart

### Voraussetzungen
- Java 25
- Docker (optional)
- PostgreSQL **oder** Umgebungsvariablen

---

### 🔐 Umgebungsvariablen

Die Anwendung verwendet **keine Credentials im Repository**.  
Alle sensiblen Daten werden über **Umgebungsvariablen** konfiguriert:
```bash
DB_URL=jdbc:postgresql://localhost:5432/rezeptapp
DB_USER=your_username
DB_PASSWORD=your_password
```

In Production werden diese Variablen direkt in Render.com gesetzt.

---

### ▶️ Anwendung starten (lokal)
```bash
./gradlew bootRun
```

Die API ist danach erreichbar unter:
```
http://localhost:8080
```

---

### 🧪 Tests ausführen
```bash
./gradlew test
```

**Test-Report öffnen:**
```bash
open build/reports/tests/test/index.html
```

✅ Alle Tests laufen lokal und in CI erfolgreich.

---

## 📡 REST API – Endpunkte

### 🔐 Authentifizierung

| Methode | Endpoint | Beschreibung |
|---------|----------|--------------|
| POST | `/auth/register` | Nutzer registrieren |
| POST | `/auth/login` | Login (Token erhalten) |

---

### 🍽️ Rezepte

| Methode | Endpoint | Beschreibung |
|---------|----------|--------------|
| GET | `/recipes` | Alle Rezepte |
| GET | `/recipes/{id}` | Einzelnes Rezept |
| POST | `/recipes` | Rezept erstellen |
| PUT | `/recipes/{id}` | Rezept bearbeiten |
| DELETE | `/recipes/{id}` | Rezept löschen |
| POST | `/recipes/{id}/favorite` | Rezept favorisieren |

---

### 📅 Essenspläne

| Methode | Endpoint | Beschreibung |
|---------|----------|--------------|
| POST | `/rezeptapp/plans/pdf` | Essensplan als PDF exportieren |

ℹ️ Essenspläne können nicht für vergangene Wochen erstellt werden.

---

### 📊 Statistiken

| Methode | Endpoint | Beschreibung |
|---------|----------|--------------|
| POST | `/stats` | Nährwertstatistik berechnen |

---

### 📄 Beispiel: Essensplan als PDF exportieren
```bash
curl -X POST http://localhost:8080/rezeptapp/plans/pdf \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Wochenplan",
    "weekStartMonday": "2026-01-13",
    "entries": []
  }'
```

---

## 📁 Projektstruktur
```
src/main/java/com/example/rezeptapp
├── config
│   ├── CorsConfig
│   ├── DataSeeder
│   ├── GlobalExceptionHandler
│   └── SecurityCryptoConfig
│
├── controller
│   ├── AuthController
│   ├── MealPlanController
│   ├── RecipeController
│   └── StatsController
│
├── model
│   ├── Ingredient
│   ├── MealPlan
│   ├── MealPlanEntry
│   ├── MealSlot
│   ├── Nutrition
│   ├── Recipe
│   └── UserAccount
│
├── repository
│   ├── RecipeRepository
│   └── UserAccountRepository
│
├── service
│   ├── AuthService
│   ├── MealPlanPdfService
│   ├── MealPlanService
│   ├── PdfService
│   ├── RecipeService
│   └── StatsService
│
└── RezeptappApplication.java
```

---

## ❗ Fehlerbehandlung

### Einheitliches Fehlerformat

Das Backend verwendet einen zentralen `GlobalExceptionHandler`:
- `IllegalArgumentException` → 400 Bad Request
- Authentifizierungsfehler → 401 Unauthorized
- Nicht gefundene Ressourcen → 404 Not Found
- Unerwartete Fehler → 500 Internal Server Error

---

### ⚠️ Bewusste Ausnahme (dokumentiert)

Der `AuthController` gibt bei fehlendem oder ungültigem Token explizit **401 Unauthorized** zurück, um Authentifizierungsfehler klar von Validierungsfehlern zu trennen.

Diese Abweichung vom einheitlichen Fehlerformat ist:
- bewusst implementiert
- dokumentiert
- testabgedeckt

---

## 🧪 Tests

Das Backend enthält umfangreiche Unit- und Integrationstests:

| Testklasse | Typ |
|------------|-----|
| AuthControllerTest | Controller |
| RecipeControllerTest | Controller |
| MealPlanControllerTest | Controller |
| StatsControllerTest | Controller |
| GlobalExceptionHandlerTest | Fehlerhandling |
| AuthServiceTest | Service |
| RecipeServiceTest | Service |
| MealPlanServiceTest | Service |
| StatsServiceTest | Service |

✔️ Alle Tests laufen lokal und in GitHub Actions CI erfolgreich.

---

## 🔄 CI/CD

GitHub Actions führt bei jedem Push automatisch aus:
- Build mit Gradle
- Alle Backend-Tests
- Abbruch bei Fehlern

**Workflow:**  
`.github/workflows/backend.yml`

---

## 🌍 Deployment

Das Backend ist auf **Render.com** deployed:
- Docker-basiertes Deployment
- Auto-Deploy bei Push auf `main`
- Umgebungsvariablen in Render gesetzt
- Keine Klartext-Credentials im Repository

---

## 🐳 Docker
```dockerfile
FROM eclipse-temurin:25-jdk-jammy AS build
WORKDIR /app
COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew
RUN ./gradlew --no-daemon dependencies || true
COPY . .
RUN ./gradlew build --no-daemon
FROM eclipse-temurin:25-jdk-jammy
WORKDIR /app
COPY --from=build /app/build/libs/rezeptapp-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
```

---

## 📝 Hinweis

Dieses Projekt wurde im Rahmen des Moduls **Webtechnologien** an der **HTW Berlin** umgesetzt.

---