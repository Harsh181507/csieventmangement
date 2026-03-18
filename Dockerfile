
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app


COPY pom.xml .
RUN mvn dependency:go-offline --no-transfer-progress

COPY src ./src
RUN mvn clean package -DskipTests --no-transfer-progress


FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=builder /app/target/csieventmangement.jar app.jar


EXPOSE 8080

ENTRYPOINT ["java", \
            "-XX:+UseContainerSupport", \
            "-XX:MaxRAMPercentage=75.0", \
            "-jar", "app.jar"]