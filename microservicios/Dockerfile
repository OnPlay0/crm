# Etapa 1: Build del JAR
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Etapa 2: Imagen final liviana
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/microservicios-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
