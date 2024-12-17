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

ARG spring.application.name
ARG spring.data.mongodb.database
ARG spring.data.mongodb.uri

ARG spring.servlet.multipart.max-file-size=16MB
ARG spring.servlet.multipart.max-request-size=100MB
ARG server.tomcat.max-swallow-size=100MB

ARG drip-app.jwt-secret

ARG drip-app.jwt-expiration=86400000
ARG drip-app.admin-username
ARG drip-app.admin-password
ARG drip-app.client-origin
ARG drip-app.server-origin
ARG drip-app.paypal-client-id
ARG drip-app.paypal-client-secret

ARG spring.mail.host
ARG spring.mail.port=587
ARG spring.mail.username
ARG spring.mail.password
ARG spring.mail.properties.mail.smtp.auth=true
ARG spring.mail.properties.mail.smtp.starttls.enable=true
ARG spring.mail.properties.mail.smtp.ssl.trust

CMD ["java", "-jar", "app.jar"]