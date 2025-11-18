# Primera etapa: "builder". Usa una imagen con JDK completo (para compilar/build).
FROM eclipse-temurin:21-jdk-jammy AS builder

# Define el directorio de trabajo dentro del contenedor (todas las rutas serán relativas a /app).
WORKDIR /app

# Copia TODO el proyecto desde tu máquina (contexto de build) al directorio /app del contenedor.
COPY . .

# Le da permiso de ejecución al script de Gradle (gradlew) para poder correrlo.
RUN chmod +x ./gradlew

# Ejecuta un comando RUN más complejo usando secretos para autenticarse en GitHub Packages
RUN --mount=type=secret,id=gpr.user \
    --mount=type=secret,id=gpr.key \
    mkdir -p /root/.gradle && \
    echo "gpr.user=$(cat /run/secrets/gpr.user)" >> /root/.gradle/gradle.properties && \
    echo "gpr.key=$(cat /run/secrets/gpr.key)"  >> /root/.gradle/gradle.properties && \
    ./gradlew --no-daemon clean bootJar && \
    rm -f /root/.gradle/gradle.properties
#monta los secrets y los escribe en el properties, ejecuta Gradle para compilar el proyecto y generar el jar de Spring boot
# y despues borra el gradle.properties para que los secrets no queden en la imagen final

# Segunda etapa: imagen de runtime. Más liviana: solo JRE (sin herramientas de compilación).
# "Copia el jar como app.jar."
FROM eclipse-temurin:21-jre-jammy

# Instala curl dentro de la imagen de runtime (lo podés usar para healthchecks, debug, etc.).
# Después borra las listas de apt para que la imagen sea más liviana.
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Define el directorio de trabajo en la imagen de runtime.
WORKDIR /app

# Copia el .jar generado en la etapa "builder" a /app/app.jar
COPY --from=builder /app/build/libs/*.jar app.jar

# Expone el puerto 8080 (el que usa tu app Spring Boot adentro del contenedor).
EXPOSE 8080

# Copia el agente de New Relic (newrelic.jar) al directorio /app dentro de la imagen.
COPY newrelic/newrelic.jar /app/newrelic.jar
# Copia el archivo de configuración de New Relic (newrelic.yml) al directorio /app.
COPY newrelic/newrelic.yml /app/newrelic.yml
# ENTRYPOINT define el comando que se ejecuta cuando se levanta el contenedor
ENTRYPOINT ["java", "-Dspring.profiles.active=production", "-javaagent:/app/newrelic.jar", "-jar", "/app/app.jar"]
#ejecuta la JVM, activa el profile production de Spring, carga el agente new relic como javaagent
# indica que va a ejecutar un jar y la ruta al jar de la applicacion