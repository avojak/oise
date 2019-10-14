# Open IRC Search Engine (OISE)

OISE is an open-source search engine for IRC servers and channels.

## Building

```bash
$ mvn install dockerfile:build
```

## Running

```bash
$ docker run -it -p 8080:8080 avojak/oise:latest
```

### Configuration

The following environment variables may be optionally overridden at runtime:

| Environment Variable | Default Value |
| -------------------- | ------------- |
| TODO                 |               |