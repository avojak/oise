package com.avojak.webapp.oise.model;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

public class ChannelResult {

	private final String channel;
	private final int numUsers;
	private final String topic;
	private final String webContent;

	private String channelNameTokens = "";

	public ChannelResult(final String channel, final int numUsers, final String topic, final String webContent) {
		this.channel = checkNotNull(channel);
		this.numUsers = numUsers;
		this.topic = checkNotNull(topic);
		this.webContent = checkNotNull(webContent);
	}

	public String getChannel() {
		return channel;
	}

	public int getNumUsers() {
		return numUsers;
	}

	public String getTopic() {
		return topic;
	}

	public String getWebContent() {
		return webContent;
	}

	@Override
	public String toString() {
		return "ChannelResult{" +
				"channel='" + channel + '\'' +
				", numUsers=" + numUsers +
				", topic='" + topic + '\'' +
				", webContent='" + webContent + '\'' +
				", channelNameTokens='" + channelNameTokens + '\'' +
				'}';
	}
}
