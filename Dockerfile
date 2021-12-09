FROM openjdk:11-jre-slim

ADD target/srijan-demo.jar app.jar

EXPOSE 8090

ENTRYPOINT ["java", "-jar", "app.jar"]
