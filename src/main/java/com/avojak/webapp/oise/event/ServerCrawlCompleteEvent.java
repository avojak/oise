package com.avojak.webapp.oise.event;

import com.avojak.webapp.oise.model.ChannelListing;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The event to be sent when crawling completes for a server.
 */
public class ServerCrawlCompleteEvent {

	private final String server;
	private final List<ChannelListing> channelListings;

	public ServerCrawlCompleteEvent(final String server, final List<ChannelListing> channelListings) {
		this.server = checkNotNull(server);
		this.channelListings = checkNotNull(channelListings);
	}

	public String getServer() {
		return server;
	}

	public List<ChannelListing> getChannelListings() {
		return channelListings;
	}
}
