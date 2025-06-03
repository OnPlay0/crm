# Etapa 1: Construcción del JAR
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Etapa 2: Imagen liviana para ejecución
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/leads-0.0.1-SNAPSHOT.jar app.jar


EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
