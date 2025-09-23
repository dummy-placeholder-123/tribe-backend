# syntax=docker/dockerfile:1

# Build stage: compile the Spring Boot application and produce the runnable jar.
FROM maven:3.9.8-eclipse-temurin-17 AS build
WORKDIR /workspace

# Leverage Docker layer caching by copying the pom first.
COPY pom.xml ./
# Copy source code and resolve dependencies / build in a single step.
COPY src ./src
RUN mvn -B -DskipTests package "-Dspring.profiles.active=test"

# Runtime stage: run the application with a slim JRE image.
FROM eclipse-temurin:17.0.13_11-jre-alpine
WORKDIR /app

# Copy the fat jar produced during the build stage.
COPY --from=build /workspace/target/spring-webapi-0.0.1-SNAPSHOT.jar app.jar

# Expose the default port Spring Boot listens on.
EXPOSE 8080

# Use a non-root user for better container security.
RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring:spring

# Propagate runtime metadata to the container.
ENV JAVA_OPTS=""
ENV SPRING_PROFILES_ACTIVE=test
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
