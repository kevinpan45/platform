FROM ghcr.io/kevinpan45/action-box:latest
RUN mvn clean package -DskipTests


FROM openjdk:17-jdk-slim-buster
WORKDIR /app

COPY --from=build target/*.jar ./

ENTRYPOINT java -jar app.jar