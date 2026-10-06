
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app


COPY pom.xml .
RUN mvn dependency:go-offline --no-transfer-progress

COPY src ./src
RUN mvn clean package -DskipTests --no-transfer-progress


FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Run as a non-root user
RUN useradd --system --uid 1001 appuser
USER appuser

COPY --from=builder /app/target/csieventmangement.jar app.jar


EXPOSE 8080

# Tuned for small containers (Render free/starter: 512 MB RAM):
#  - SerialGC has the lowest memory and CPU overhead on 1 vCPU or less
#  - smaller thread stacks, and exit on OOM so Render restarts the service
ENTRYPOINT ["java", \
            "-XX:+UseContainerSupport", \
            "-XX:MaxRAMPercentage=70.0", \
            "-XX:+UseSerialGC", \
            "-Xss512k", \
            "-XX:+ExitOnOutOfMemoryError", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-jar", "app.jar"]
