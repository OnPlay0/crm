# Etapa 1: Construcción del JAR con Maven
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app
# Copiamos sólo pom y fuentes antes para aprovechar caché
COPY pom.xml mvnw ./
COPY .mvn .mvn
COPY src src
RUN mvn clean package -DskipTests

# Etapa 2: Imagen ligera para ejecución
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/cliente-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8085
ENV SPRING_CONFIG_IMPORT=optional:configtree:/app/config/
ENV SERVER_PORT=8085
ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=8085"]

