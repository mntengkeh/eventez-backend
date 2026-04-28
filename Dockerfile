FROM eclipse-temurin:24-jdk-alpine AS build
WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

# give permission to run the gradle wrapper
RUN chmod +x ./gradlew

# cpy the source code
COPY src src

RUN ./gradlew clean bootJar -DskipTests

# use a lighter JRE image because  compiler is no longer needed
FROM eclipse-temurin:24-jre-alpine
WORKDIR /app

VOLUME /tmp

COPY --from=build /app/build/libs/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=staging

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

EXPOSE 8080
