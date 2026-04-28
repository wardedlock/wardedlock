# ==============================================================================
# MULTI-STAGE DYNAMIC MONOREPO DOCKERFILE
# ==============================================================================
# This Dockerfile allows building any microservice in the monorepo by passing 
# the --build-arg MODULE_NAME=<module> during the docker build process.
#
# Example usage:
# docker build --build-arg MODULE_NAME=auth-service -t wardedlock-auth:latest .
# ==============================================================================

# ------------------------------------------------------------------------------
# Stage 1: Builder
# ------------------------------------------------------------------------------
FROM eclipse-temurin:25-jdk-alpine AS builder

WORKDIR /app

# Copy the Gradle Wrapper and configurations for independent building
COPY gradlew .
COPY gradle gradle
COPY buildSrc buildSrc
COPY settings.gradle.kts .

# Grant execution rights to the wrapper
RUN chmod +x ./gradlew

# Resolve dependencies for buildSrc to cache them in the Docker layer
RUN ./gradlew :buildSrc:classes --no-daemon

# Copy the entire project source code
COPY . .

# Accept the target module name as an argument
ARG MODULE_NAME
ENV MODULE_NAME=${MODULE_NAME}

# Validate that MODULE_NAME was provided
RUN if [ -z "$MODULE_NAME" ]; then echo "ERROR: MODULE_NAME argument is required. Use --build-arg MODULE_NAME=<module>" && exit 1; fi

# Build the specific module, skipping tests to optimize build time
RUN ./gradlew :${MODULE_NAME}:bootJar --no-daemon -x test

# Extract the generated execution JAR file
RUN cp ${MODULE_NAME}/build/libs/${MODULE_NAME}-*.jar application.jar

# ------------------------------------------------------------------------------
# Stage 2: Runner
# ------------------------------------------------------------------------------
FROM eclipse-temurin:25-jre-alpine

ARG MODULE_NAME
LABEL module=${MODULE_NAME}
LABEL maintainer="trunglcct@gmail.com"

# Create a non-root application user for enhanced security
RUN addgroup -S wardedlock && adduser -S wardedlock -G wardedlock
USER wardedlock:wardedlock

WORKDIR /app

# Copy the application JAR from the builder stage
COPY --from=builder /app/application.jar ./application.jar

# Expose the standard communication port
EXPOSE 8080

# Start the Spring Boot application
ENTRYPOINT ["java", "-jar", "application.jar"]
