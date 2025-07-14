FROM openjdk:21-jre-slim

WORKDIR /app

# Copy the built jar file
COPY target/runnify-*.jar app.jar

# Create uploads directory
RUN mkdir -p /app/uploads

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"] 