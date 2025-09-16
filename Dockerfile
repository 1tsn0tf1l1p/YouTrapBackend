# --- Build Stage ---
# Use an official OpenJDK 17 image as a parent image for building
FROM eclipse-temurin:17-jdk-jammy as builder

# Set the working directory inside the container
WORKDIR /workspace

# Copy the Gradle wrapper and build files
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Copy the application source code
COPY src ./src

# Make the gradlew script executable
RUN chmod +x ./gradlew

# Build the application and create the executable JAR.
# --no-daemon is recommended for CI/CD environments.
RUN ./gradlew bootJar --no-daemon


# --- Run Stage ---
# Use a smaller JRE image for the final container to reduce size
FROM eclipse-temurin:17-jre-jammy

# Set the working directory
WORKDIR /app

# Copy the executable JAR from the builder stage
COPY --from=builder /workspace/build/libs/*.jar app.jar

# Expose the port the application will run on. Render will use this.
EXPOSE 8080

# The command to run the application when the container starts.
# Secrets will be passed in as environment variables by Render.
ENTRYPOINT ["java", "-jar", "app.jar"]