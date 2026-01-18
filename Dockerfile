# BUILD
FROM eclipse-temurin:25-jdk-jammy AS build

WORKDIR /app

# erst Gradle Wrapper + Buildfiles kopieren
COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle settings.gradle ./

# Wrapper executable machen
RUN chmod +x gradlew

# Dependencies einmal vorladen (schneller bei späteren Builds)
RUN ./gradlew --no-daemon dependencies || true

# dann erst den Rest (Source)
COPY . .

# Build
RUN ./gradlew build --no-daemon

# RUN
FROM eclipse-temurin:25-jdk-jammy
WORKDIR /app
COPY --from=build /app/build/libs/rezeptapp-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]