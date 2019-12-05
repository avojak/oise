<p align="center">
  <img src="src/main/resources/static/chat.svg" height="64" width="64" alt="Icon" />
</p>
<h1 align="center">Open IRC Search Engine (OISE)</h1>

<p align="enter">OISE is an open-source search engine for IRC servers and channels.</p>

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