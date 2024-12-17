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

ARG DRIP_SERVER_NAME
ARG MONGODB_NAME
ARG MONGODB_URI
ARG JWT_SECRET

ARG ADMIN_USERNAME
ARG ADMIN_PASSWORD
ARG CLIENT_URL
ARG SERVER_URL
ARG PAYPAL_CLIENT_ID
ARG PAYPAL_CLIENT_SECRET

ARG MAIL_HOST
ARG MAIL_PORT
ARG MAIL_USERNAME
ARG MAIL_PASSWORD

CMD ["java", "-jar", "app.jar"]