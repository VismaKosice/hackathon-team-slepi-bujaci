# Build stage
FROM registry.access.redhat.com/ubi9/openjdk-25:1.24 AS builder

# Install gzip and tar utilities needed by Maven wrapper
USER 0
RUN microdnf install -y gzip tar && microdnf clean all
USER 185

WORKDIR /app

# Copy Maven wrapper and pom.xml first for better caching
COPY --chown=185 mvnw .
COPY --chown=185 .mvn .mvn
COPY --chown=185 pom.xml .

# Download dependencies (cached layer)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY --chown=185 src src

# Build the application
RUN ./mvnw package -DskipTests -B

# Runtime stage
FROM registry.access.redhat.com/ubi9/openjdk-25:1.24

ENV LANGUAGE='en_US:en'

# We make four distinct layers so if there are application changes the library layers can be re-used
COPY --chown=185 --from=builder /app/target/quarkus-app/lib/ /deployments/lib/
COPY --chown=185 --from=builder /app/target/quarkus-app/*.jar /deployments/
COPY --chown=185 --from=builder /app/target/quarkus-app/app/ /deployments/app/
COPY --chown=185 --from=builder /app/target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8080
USER 185
ENV JAVA_OPTS_APPEND="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

ENTRYPOINT [ "/opt/jboss/container/java/run/run-java.sh" ]

