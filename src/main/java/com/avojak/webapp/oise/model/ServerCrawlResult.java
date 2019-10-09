package com.avojak.webapp.oise.model;

import java.util.List;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

public class ServerCrawlResult {

	private final String server;
	private final List<ChannelTopicResult> channelTopicResults;

	public ServerCrawlResult(final String server, final List<ChannelTopicResult> channelTopicResults) {
		this.server = checkNotNull(server);
		this.channelTopicResults = checkNotNull(channelTopicResults);
	}

	public String getServer() {
		return server;
	}

	public List<ChannelTopicResult> getChannelTopicResults() {
		return channelTopicResults;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ServerCrawlResult that = (ServerCrawlResult) o;
		return Objects.equals(server, that.server) &&
				Objects.equals(channelTopicResults, that.channelTopicResults);
	}

	@Override
	public int hashCode() {
		return Objects.hash(server, channelTopicResults);
	}

	@Override
	public String toString() {
		return "ServerCrawlResult{" +
				"server='" + server + '\'' +
				", channelTopicResults=" + channelTopicResults +
				'}';
	}
	
}
