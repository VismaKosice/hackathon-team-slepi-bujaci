###
# This Dockerfile is used to build a container that runs the Quarkus application in JVM mode
# optimized for fast startup time
###
FROM registry.access.redhat.com/ubi9/openjdk-17:1.23

ENV LANGUAGE='en_US:en'

# Copy the application
COPY --chown=185 target/quarkus-app/lib/ /deployments/lib/
COPY --chown=185 target/quarkus-app/*.jar /deployments/
COPY --chown=185 target/quarkus-app/app/ /deployments/app/
COPY --chown=185 target/quarkus-app/quarkus/ /deployments/quarkus/

# Expose port 8080 (can be overridden by PORT env variable)
EXPOSE 8080
USER 185

# Set JVM options for fast startup
ENV JAVA_OPTS_APPEND="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

ENTRYPOINT [ "/opt/jboss/container/java/run/run-java.sh" ]
