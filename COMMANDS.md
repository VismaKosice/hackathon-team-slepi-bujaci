run: `./mvnw quarkus:dev`

build jar: `./mvnw package -DskipTests`

build image: `docker build -f src/main/docker/Dockerfile.jvm -t quarkus/hyperspeed-quarkus-jvm .`

run container: `docker compose up`

load test: ``
