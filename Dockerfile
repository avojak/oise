FROM openjdk:8-jdk-alpine

ARG DEPENDENCY=target/dependency

COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app
COPY servers.txt /

RUN mkdir /lucene-index

EXPOSE 8080

ENTRYPOINT [ "java", "-cp", "app:app/lib/*", "com.avojak.webapp.oise.Application", "--oise.serversfile=/servers.txt", "--oise.index.directory=/lucene-index" ]