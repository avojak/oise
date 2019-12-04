FROM openjdk:8-jre-alpine

ARG DEPENDENCY=target/dependency

# Copy the application and all required files
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app
COPY servers.txt /
COPY docker-entrypoint.sh /

# Install curl for use in the health check
RUN apk --no-cache add curl

# Create the service user
RUN addgroup -S oise
RUN adduser -S oise -G oise

RUN chmod +x /docker-entrypoint.sh
RUN mkdir /lucene-index

# Update file ownership
RUN chown oise:oise /app /docker-entrypoint.sh /lucene-index

VOLUME [ "/lucene-index" ]

EXPOSE 8080

USER oise

HEALTHCHECK CMD curl --fail http://localhost:8080/actuator/health || exit 1

ENTRYPOINT [ "/docker-entrypoint.sh" ]