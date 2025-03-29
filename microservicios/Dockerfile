# Usar OpenJDK 17
FROM openjdk:17-jdk-slim

# Establecer el directorio de trabajo en el contenedor
WORKDIR /app

# Copiar el JAR de la aplicación
COPY target/microservicios-0.0.1-SNAPSHOT.jar app.jar

# Exponer el puerto 8085
EXPOSE 8085

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar" ,"--server.port=8085"]
