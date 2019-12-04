#!/bin/sh

java -cp app:app/lib/* com.avojak.webapp.oise.Application \
    --oise.serversfile=/servers.txt \
    --oise.index.directory=/lucene-index \
    --oise.crawler.max.threads=10 \
    --oise.scraper.max.threads=500 \
    --logging.level.com.avojak.webapp.oise=TRACE