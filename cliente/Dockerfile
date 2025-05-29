# Etapa 1: Construcción del JAR con Maven
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Etapa 2: Imagen liviana para ejecución
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/cliente-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENV SPRING_CONFIG_IMPORT=optional:configtree:/app/config/
ENTRYPOINT ["sh", "-c", "java -Dspring.datasource.url=$DB_URL -Dspring.datasource.username=$DB_USER -Dspring.datasource.password=$DB_PASSWORD -Djwt.secret=$JWT_SECRET -Dserver.port=$PORT -jar app.jar"]
