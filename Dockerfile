# Build stage
FROM eclipse-temurin:17-jdk-alpine AS builder
 
WORKDIR /app
COPY . .
RUN ./mvnw package
 
# Run stage
FROM eclipse-temurin:17-jdk-alpine AS runner
 
WORKDIR /app
COPY ./src/main/resources/static ./src/main/resources/static
COPY ./src/main/resources/templates ./src/main/resources/templates
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080
 
CMD ["java", "-jar", "app.jar"]