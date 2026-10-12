# ------------------------------------------------------------------------------
# Stage 1: Build stage with Maven and Eclipse Temurin JDK 21
# ------------------------------------------------------------------------------
FROM maven:3.9.9-eclipse-temurin-21-jammy AS builder

WORKDIR /build

# Cache Maven dependencies by copying pom.xml first
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copy source code and build production fat JAR
COPY src ./src
RUN mvn clean package -DskipTests -B

# ------------------------------------------------------------------------------
# Stage 2: Minimal production runtime with Eclipse Temurin JRE 21
# ------------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-jammy AS runner

WORKDIR /app

# Create a non-root system user and group for security
RUN groupadd -r app && useradd -r -g app -d /app -s /sbin/nologin app

# Copy the built fat JAR from the builder stage
COPY --from=builder --chown=app:app /build/target/*.jar app.jar

# Switch to non-root user
USER app

# EVENTTICKETINGSYSTEM backend port
EXPOSE 4040

# Configure JVM options for containerized workloads
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Run the Spring Boot application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

