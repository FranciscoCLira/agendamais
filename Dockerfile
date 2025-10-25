## Multi-stage Dockerfile for AgendaMais
# Build stage
FROM maven:3.9.0-eclipse-temurin-18-slim AS builder
WORKDIR /workspace

# Cache Maven deps
COPY pom.xml ./
RUN mvn -B -DskipTests dependency:go-offline

# Copy source and build
COPY . .
RUN mvn -B -DskipTests package -DskipTests

# Runtime stage
FROM eclipse-temurin:18-jre-jammy
RUN groupadd -r app && useradd -r -g app app
WORKDIR /app
ARG JAR_FILE=target/*.jar
COPY --from=builder /workspace/target/*.jar /app/app.jar
RUN chown app:app /app/app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
