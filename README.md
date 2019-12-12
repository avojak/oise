[![Build Status](https://travis-ci.com/avojak/oise.svg?branch=master)](https://travis-ci.com/avojak/oise)
![GitHub](https://img.shields.io/github/license/avojak/oise)
![Docker Pulls](https://img.shields.io/docker/pulls/avojak/oise)

<p align="center">
  <img src="src/main/resources/static/chat.svg" height="64" width="64" alt="Icon" />
</p>
<h1 align="center">Open IRC Search Engine (OISE)</h1>

<p align="center">OISE is an open-source search engine for IRC servers and channels.</p>

## Building

```bash
$ mvn install dockerfile:build
```

## Running

```bash
$ docker run -it --name oise -p 8080:8080 avojak/oise:latest
```

It takes a very long time for a new index to be created, so it is recommended to mount a volume to persist the index.
This will allow you to restart the container and have immediate access to the previous index while the new one is being
created.

```bash
$ docker run \
    -it \
    --name oise \
    -v lucene-index:/lucene-index \
    -p 8080:8080 \
    avojak/oise:latest
```

If you would like to customize the list of servers that are indexed, you can mount your own server list.

For example, a much shorter file (servers.txt) has been provided in this repository:
```
irc.freenode.net
irc.bsdunix.us
```

```bash
$ docker run \
    -it \
    --name oise \
    -v lucene-index:/lucene-index \
    -v <working_directory>/oise/servers.txt:/servers.txt \
    -p 8080:8080 \
    avojak/oise:latest
```

## Usage

### REST API

Once running, the API documentation is viewable at [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html).

#### Example Request

```bash
curl -X GET http://localhost:8080/api/v1/search?q=uiuc
```

### Webpage Search

A webpage for UI-based search is also available at http://localhost:8080. Simply type your query in the search field and
hit <kbd>Enter</kbd>, or select the "Search" button.

## Implementation Details

<img src="./assets/architecture.png" />

OISE is implemented with Spring Boot as the application framework, and each key component of the application (crawling 
IRC servers and indexing channels) is implemented as a Guava service.

### Services

All services are started in the background when the application starts (see: `ApplicationRunner`).

#### Crawling Service

The crawling service is scheduled to run every 24 hours. An IRC bot (PircBot) is created for each server to be crawled,
and then a crawling thread is submitted for background execution.

Once crawling completes, the raw text response from the IRC servers is converted into POJOs and any URLs are scraped for
additional content to supplement the channel topic. Finally, the collection of models representing each channel found on
the server is sent to the IndexingService for processing.

#### Indexing Service

The indexing service is idle until it receives an event from the crawling service that a crawl action of a server has 
completed. A background thread is submitted for background execution to process the index update. In our index the
"documents" representing each IRC channel contain the following fields:

- The server
- The channel name
- The topic
- The web content of all URLs present in the topic
- The number of users

Only the channel name, topic and web content are considered during queries, but the other data is also available.

### REST API

The REST API has a single endpoint: `/api/v1/search` (see: `SearchController`). The endpoint accepts a single query parameter (`q`) which contains the query.

An example query URL would look like: `http://localhost:8080/api/v1/search?q=test`.

The response contains all the data from the index for the 10 most relevant results. For example:

```json
[
  {
    "server": "irc.freenode.net",
    "channel": "#QuantumZNC-Test",
    "topic": "#QuantumZNC-Test",
    "urlContent": "",
    "users": 5,
    "scoreDoc": {
      "score": 6.029067,
      "doc": 12987,
      "shardIndex": 0,
      "fields": [
        6.029067
      ]
    }
  },
  ...
]
```

### Configuration

Several parameters are available for configuration in `application.properties`:

```properties
oise.serversfile=servers.txt # The file containing the list of servers
oise.index.directory=index/  # The directory containing the Apache Lucene index
oise.crawler.max.threads=10  # The maximum number of threads used for crawling servers for channels
oise.scraper.max.threads=500 # The maximum number of threads used for scraping web content from URLs mentioned in topics
```

<hr>

<small>OISE was developed to satisfy the Final Project requirement for [CS410: Text Information Systems](https://cs.illinois.edu/courses/profile/CS410) at the [University of Illinois at Urbana-Champaign](https://cs.illinois.edu/)</small>