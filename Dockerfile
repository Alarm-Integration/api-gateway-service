FROM adoptopenjdk/openjdk11:alpine-jre
COPY build/libs/*.jar app.jar
CMD ["java", "-jar", "-Xmx1024m", "/app.jar"]
