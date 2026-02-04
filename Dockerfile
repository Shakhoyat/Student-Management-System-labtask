# Base image: Uses Eclipse Temurin JDK 17 (official OpenJDK distribution)
FROM eclipse-temurin:17-jdk

# Set working directory inside the container
WORKDIR /app

# Copy the compiled JAR file from host's target/ directory to container
COPY target/*.jar app.jar

# Define the command to run when container starts (exec form for proper signal handling)
ENTRYPOINT ["java","-jar","app.jar"]