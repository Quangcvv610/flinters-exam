FROM eclipse-temurin:21-jdk-jammy
ARG JAR_FILE=target/flinters-exam-0.0.1-SNAPSHOT.jar
ADD ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
EXPOSE 8001