# Use official OpenJDK image
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copy project files
COPY . .

# Build the application
RUN ./mvnw clean package -DskipTests || mvn clean package -DskipTests

# Expose port
EXPOSE 8080

# Run the jar
CMD ["sh", "-c", "java -jar target/*.jar"]
