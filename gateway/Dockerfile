# gateway/Dockerfile

# Etapa 1: Build del JAR con Maven
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app

# Copiamos primero solo pom y scripts para cachear dependencias
COPY pom.xml mvnw ./
COPY .mvn .mvn
# Ahora copiamos el resto de código
COPY src src

# Compilamos sin tests
RUN mvn clean package -DskipTests

# Etapa 2: Imagen ligera para ejecución
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copiamos el JAR generado en la etapa 'build'
COPY --from=build /app/target/*.jar app.jar

# Puerto que expones en docker-compose y en tu gateway config
EXPOSE 8080

# Para cargar configuraciones externas en /app/config
ENV SPRING_CONFIG_IMPORT=optional:configtree:/app/config/

ENTRYPOINT ["java", "-jar", "app.jar"]
