# --- STAGE 1: Build ---
# We use a full JDK image to compile the Kotlin code
FROM eclipse-temurin:24-jdk-alpine AS build
WORKDIR /app

# 1. Copy the build configuration files first
# (This allows Docker to cache dependencies so builds are faster)
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

# 2. Give permission to run the gradle wrapper
RUN chmod +x ./gradlew

# 3. Copy the source code
COPY src src

# 4. Build the executable JAR (skipping tests for speed in staging)
RUN ./gradlew clean bootJar -DskipTests

# --- STAGE 2: Run ---
# We use a lighter JRE (Runtime) image because we don't need the compiler anymore
FROM eclipse-temurin:24-jre-alpine
WORKDIR /app

# 5. Create a temporary volume for Spring Boot's Tomcat logs/files
VOLUME /tmp

# 6. Copy the compiled JAR from the 'build' stage
COPY --from=build /app/build/libs/*.jar app.jar

# 7. Set the active profile to 'staging'
ENV SPRING_PROFILES_ACTIVE=staging

# 8. Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

# 9. Inform Docker that the app listens on 8080
EXPOSE 8080