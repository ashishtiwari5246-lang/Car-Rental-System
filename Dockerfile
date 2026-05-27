FROM eclipse-temurin:17-jdk-alpine
COPY  target/car-rental-system-1.0.0.jar myapp.jar

ENTRYPOINT ["java", "-jar","myapp.jar"]