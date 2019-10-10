package com.avojak.webapp.oise.model;

import java.util.List;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

public class ServerCrawlerResult {

	private final String server;
	private final List<ChannelResult> channelResults;

	public ServerCrawlerResult(final String server, final List<ChannelResult> channelResults) {
		this.server = checkNotNull(server);
		this.channelResults = checkNotNull(channelResults);
	}

	public String getServer() {
		return server;
	}

	public List<ChannelResult> getChannelResults() {
		return channelResults;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ServerCrawlerResult that = (ServerCrawlerResult) o;
		return Objects.equals(server, that.server) &&
				Objects.equals(channelResults, that.channelResults);
	}

	@Override
	public int hashCode() {
		return Objects.hash(server, channelResults);
	}

	@Override
	public String toString() {
		return "ServerCrawlerResult{" +
				"server='" + server + '\'' +
				", channelResults=" + channelResults +
				'}';
	}

}
