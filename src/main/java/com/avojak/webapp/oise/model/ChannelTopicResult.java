package com.avojak.webapp.oise.model;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

public class ChannelTopicResult {

	private final String channel;
	private final int numUsers;
	private final String topic;

	public ChannelTopicResult(final String channel, final int numUsers, final String topic) {
		this.channel = checkNotNull(channel);
		this.numUsers = numUsers;
		this.topic = checkNotNull(topic);
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ChannelTopicResult that = (ChannelTopicResult) o;
		return numUsers == that.numUsers &&
				Objects.equals(channel, that.channel) &&
				Objects.equals(topic, that.topic);
	}

	@Override
	public int hashCode() {
		return Objects.hash(channel, numUsers, topic);
	}

	@Override
	public String toString() {
		return "ChannelTopicResult{" +
				"channel='" + channel + '\'' +
				", numUsers=" + numUsers +
				", topic='" + topic + '\'' +
				'}';
	}
	
}
