FROM openjdk:8-jdk-alpine

ARG DEPENDENCY=target/dependency

# Mount the OISE directory for data persistance
VOLUME /var/local/oise

# Create the directory for Lucene to store the index
# TODO: Do this elsewhere, it won't work here because we're mounting it
# RUN mkdir -p /var/local/oise/lucene-index/

COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app
COPY docker-entrypoint.sh /
    
EXPOSE 8080
