FROM openjdk:22-jdk
COPY target/PopcornDNBot-1.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]