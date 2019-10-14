FROM openjdk:8-jdk-alpine

ARG DEPENDENCY=target/dependency

COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app
COPY servers.txt /
COPY docker-entrypoint.sh /

RUN mkdir /lucene-index

EXPOSE 8080

ENTRYPOINT [ "/docker-entrypoint.sh" ]