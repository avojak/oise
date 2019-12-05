[![Build Status](https://travis-ci.com/avojak/oise.svg?branch=master)](https://travis-ci.com/avojak/oise)
![GitHub](https://img.shields.io/github/license/avojak/oise)

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

For example, a much shorter file (/tmp/servers.txt on your local system):
```
irc.freenode.net
irc.bsdunix.us
```

```bash
$ docker run \
    -it \
    --name oise \
    -v lucene-index:/lucene-index \
    -v /tmp/servers.txt:/servers.txt \
    -p 8080:8080 \
    avojak/oise:latest
```

## API Usage

Once running, the API documentation is viewable at [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html).

### Example Request

```bash
curl -X GET http://localhost:8080/api/v1/search?q=uiuc
```