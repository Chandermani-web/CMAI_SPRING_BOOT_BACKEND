# --- build ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B -DskipTests package

# --- run ---
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/cmai-backend-1.0.0.jar app.jar
# Provide secrets as environment variables (or mount .env / serviceAccountKey.json into /app).
EXPOSE 8000
ENTRYPOINT ["java", "-jar", "app.jar"]
