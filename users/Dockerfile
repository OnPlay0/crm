FROM openjdk:17-jdk-slim

WORKDIR /app

# Copiar el .jar
COPY target/users-0.0.1-SNAPSHOT.jar app.jar

# Copiar el application.yml que ya viene dentro del jar (Spring lo encuentra solo)
# No copiarlo manualmente

EXPOSE 8081

# Ejecutar normalmente, sin forzar config.location
ENTRYPOINT ["java", "-jar", "app.jar"]
