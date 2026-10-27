FROM gradle:8.5.0-jdk21 AS build
COPY --chown=gradle:gradle . /home/gradle/project
WORKDIR /home/gradle/project
RUN chmod +x gradlew
RUN ./gradlew clean bootJar

FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=build /home/gradle/project/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
