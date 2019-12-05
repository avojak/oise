#!/bin/sh

# TODO: Make this configurable at runtime via environment variables
java -cp app:app/lib/* com.avojak.webapp.oise.Application \
    --spring.application.name="Open IRC Search Engine" \
    --oise.serversfile=/servers.txt \
    --oise.index.directory=/lucene-index \
    --oise.crawler.max.threads=10 \
    --oise.scraper.max.threads=500 \
    --logging.level.com.avojak.webapp.oise=TRACE