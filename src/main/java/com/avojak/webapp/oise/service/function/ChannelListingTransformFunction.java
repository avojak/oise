package com.avojak.webapp.oise.service.function;

import com.avojak.webapp.oise.model.ChannelListing;
import com.google.common.base.Function;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Implementation of {@link Function} to transform the raw channel messages from the IRC server into models that we can
 * process.
 */
public class ChannelListingTransformFunction implements Function<List<String>, List<ChannelListing>> {

	private final String server;

	public ChannelListingTransformFunction(final String server) {
		this.server = checkNotNull(server);
	}

	/**
	 * Transforms the message of the format:
	 * <pre>NICKNAME CHANNEL NUM_USERS :TOPIC</pre>
	 * into a model of type {@link ChannelListing}.
	 */
	@Override
	public List<ChannelListing> apply(final List<String> input) {
		final List<ChannelListing> channelListings = new ArrayList<>();
		for (final String message : input) {
			// Grab our nickname and strip it out of the message
			final String nickname = message.split(" ")[0];
			final String trimmed = message.replace(nickname, "").trim();
			// With the nickname gone, the channel is the next element of the message, followed by the number of users
			final String channel = trimmed.split(" ")[0];
			final int numUsers = Integer.parseInt(trimmed.split(" ")[1]);
			// Everything after the first ':' is the channel topic topic
			final String topic = trimmed.substring(trimmed.indexOf(":") + 1);
			channelListings.add(new ChannelListing(server, channel, topic, numUsers));
		}
		return channelListings;
	}

}
