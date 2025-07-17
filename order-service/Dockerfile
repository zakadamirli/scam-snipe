FROM eclipse-temurin:17-jre-alpine

# Working directory yaradır
WORKDIR /app

# Jar faylını container-ə kopyalayır
COPY target/*.jar app.jar

# Port-u expose edir
EXPOSE 8081

# Application-ı başladır
ENTRYPOINT ["java", "-jar", "app.jar"]