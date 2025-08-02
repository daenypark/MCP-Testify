FROM openjdk:17-jdk-slim

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Download Datadog Java Agent
RUN mkdir -p /opt/datadog && \
    curl -Lo /opt/datadog/dd-java-agent.jar https://dtdg.co/latest-java-tracer

# Set working directory
WORKDIR /app

# Copy Maven files for dependency caching
COPY pom.xml .
COPY src ./src

# Install Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Build the application
RUN mvn clean package -DskipTests

# Create logs directory
RUN mkdir -p /app/logs

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application with Datadog Java agent
ENTRYPOINT ["java", "-javaagent:/opt/datadog/dd-java-agent.jar", "-jar", "target/wedding-api-1.0.0.jar"] 