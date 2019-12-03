package com.avojak.webapp.oise.model;

import org.apache.lucene.search.ScoreDoc;

import static com.google.common.base.Preconditions.checkNotNull;

public class SearchResult {

	private final String server;
	private final String channel;
	private final String topic;
	private final String urlContent;
	private final int users;
	private final ScoreDoc scoreDoc;

	public SearchResult(final String server, final String channel, final String topic, final String urlContent,
						final int users, final ScoreDoc scoreDoc) {
		this.server = checkNotNull(server);
		this.channel = checkNotNull(channel);
		this.topic = checkNotNull(topic);
		this.urlContent = checkNotNull(urlContent);
		this.users = users;
		this.scoreDoc = checkNotNull(scoreDoc);
	}

	public String getServer() {
		return server;
	}

	public String getChannel() {
		return channel;
	}

	public String getTopic() {
		return topic;
	}

	public String getUrlContent() {
		return urlContent;
	}

	public int getUsers() {
		return users;
	}

	public ScoreDoc getScoreDoc() {
		return scoreDoc;
	}
}
