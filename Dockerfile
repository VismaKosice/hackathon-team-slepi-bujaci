###
# This Dockerfile is used to build a container that runs the Quarkus application in JVM mode
# optimized for fast startup time
###
FROM eclipse-temurin:25-jdk

ENV LANGUAGE='en_US:en'

# Copy the application
COPY target/quarkus-app/lib/ /deployments/lib/
COPY target/quarkus-app/*.jar /deployments/
COPY target/quarkus-app/app/ /deployments/app/
COPY target/quarkus-app/quarkus/ /deployments/quarkus/

# Expose port 8080 (can be overridden by PORT env variable)
EXPOSE 8080

# Set JVM options for fast startup
ENV JAVA_OPTS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"

ENTRYPOINT ["java", "-jar", "/deployments/quarkus-run.jar"]
