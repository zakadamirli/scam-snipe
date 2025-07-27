# 1. Stage: Build stage
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# 2. Stage: Run stage
FROM eclipse-temurin:17-jdk
WORKDIR /app

# brevo.crt faylını konteynerə kopyala
COPY src/main/resources/certs/brevo.crt /usr/local/share/ca-certificates/brevo.crt

# Sertifikatı əlavə et və sistemin SSL sertifikatlarını yenilə
RUN apt-get update && \
    apt-get install -y ca-certificates && \
    keytool -import -noprompt -trustcacerts -alias brevo \
      -file /usr/local/share/ca-certificates/brevo.crt \
      -keystore $JAVA_HOME/lib/security/cacerts \
      -storepass changeit && \
    rm -rf /var/lib/apt/lists/*

# Tətbiqi əlavə et
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
