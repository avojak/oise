# Open IRC Search Engine (OISE)

OISE is an open-source search engine for IRC servers and channels.

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
$ docker run -it --name oise -v lucene-index:/lucene-index -p 8080:8080 avojak/oise:latest
```

### Configuration

The following environment variables may be optionally overridden at runtime:

| Environment Variable | Default Value |
| -------------------- | ------------- |
| TODO                 |               |