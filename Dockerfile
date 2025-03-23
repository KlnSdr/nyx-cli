FROM eclipse-temurin:21-jdk

WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends unzip && rm -rf /var/lib/apt/lists/*

COPY build/nyx-cli-1.1.jar /app/nyx.jar
COPY nyx-docker.sh /usr/local/bin/nyx
RUN chmod +x /usr/local/bin/nyx
