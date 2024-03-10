FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY target/*.jar app.jar
COPY libs/*.jar /libs/
ENTRYPOINT ["java","-jar","/app.jar"]
EXPOSE 8080
