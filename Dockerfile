# 1. Build-Stage: Projekt mit Gradle Wrapper bauen
FROM gradle:8.10.2-jdk21-jammy AS build

# Arbeitsverzeichnis setzen
WORKDIR /home/gradle/src

# Projektquellcode in das Image kopieren
COPY --chown=gradle:gradle . .

# Build ausführen, Tests optional überspringen
# (wenn deine Tests stabil sind, kannst du -x test auch weglassen)
RUN ./gradlew clean build -x test --no-daemon

# 2. Runtime-Stage: schlankes JDK-Image mit nur dem fertigen JAR
FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# Das gebaute JAR aus der Build-Stage kopieren
# Der Wildcard (*) ist wichtig, damit du den Namen nicht bei jeder Version anpassen musst
COPY --from=build /home/gradle/src/build/libs/*SNAPSHOT.jar app.jar

# Port dokumentieren (Spring Boot läuft standardmäßig auf 8080)
EXPOSE 8080

# Anwendung starten
ENTRYPOINT ["java", "-jar", "app.jar"]