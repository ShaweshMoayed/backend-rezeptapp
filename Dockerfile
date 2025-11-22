# 1. Build-Stage: Projekt mit Gradle Wrapper bauen
FROM gradle:jdk21-jammy AS build

# Arbeitsverzeichnis im Container
WORKDIR /home/gradle/src

# Projektquellcode in das Image kopieren
COPY --chown=gradle:gradle . .

# Build ausführen (mit Gradle Wrapper) – Tests hier optional übersprungen
RUN ./gradlew clean build -x test --no-daemon

# 2. Runtime-Stage: schlankes JDK-Image, nur das fertige JAR
FROM eclipse-temurin:21-jdk-jammy

# Arbeitsverzeichnis im Laufzeit-Container
WORKDIR /app

# Fertiges JAR aus der Build-Stage kopieren
COPY --from=build /home/gradle/src/build/libs/rezeptapp-0.0.1-SNAPSHOT.jar app.jar

# Port (nur Doku, Render ignoriert das, ist aber nice to have)
EXPOSE 8080

# Start-Kommando
ENTRYPOINT ["java", "-jar", "app.jar"]