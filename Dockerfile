FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew --no-daemon clean bootJar

#Copia el jar como app.jar.
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
EXPOSE 8080

COPY --from=builder /app/build/libs/*.jar app.jar

COPY newrelic/newrelic.jar /app/newrelic.jar

ENTRYPOINT ["java", "-Dspring.profiles.active=production", "-javaagent:/app/newrelic.jar", "-jar", "/app/app.jar"]