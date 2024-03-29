package com.avojak.webapp.oise.model;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Models a channel result "listing".
 */
public class ChannelListing {

	private final String server;
	private final String channel;
	private final String topic;
	private final int numUsers;

	private String urlContent;

	public ChannelListing(final String server, final String channel, final String topic, final int numUsers) {
		this.server = checkNotNull(server);
		this.channel = checkNotNull(channel);
		this.topic = checkNotNull(topic);
		this.numUsers = numUsers;
		urlContent = "";
	}

	public void setUrlContent(final String urlContent) {
		this.urlContent = urlContent;
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

	public int getNumUsers() {
		return numUsers;
	}

	public String getUrlContent() {
		return urlContent;
	}
}
