FROM adoptopenjdk/openjdk11:alpine-jre
COPY build/libs/*.jar app.jar
CMD ["java", "-jar", "/app.jar"]
