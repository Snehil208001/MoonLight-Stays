# Stage 1: Build Context
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app
COPY . .
RUN ./gradlew bootJar --no-daemon

# Stage 2: Production Context Layer
FROM eclipse-temurin:21-jre-jammy AS runner
WORKDIR /app
RUN useradd -m moonlightuser && chown -R moonlightuser:moonlightuser /app
USER moonlightuser

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod

HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
