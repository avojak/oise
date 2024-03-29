package com.avojak.webapp.oise.service.bot;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.PircBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implementation of {@link PircBot} to crawl a given server for the channels it contains.
 */
public class CrawlerBot extends PircBot {

	private static final Logger LOGGER = LoggerFactory.getLogger(CrawlerBot.class);

	private final AtomicBoolean isCrawling = new AtomicBoolean(false);
	private final List<String> channels = new ArrayList<>();

	/**
	 * "Crawl" the given IRC server for a listing of channels that it hosts.
	 */
	public List<String> crawl(final String server) throws IOException, IrcException, InterruptedException {
		LOGGER.debug("Connecting to server: {}", server);
		connect(server);
		isCrawling.set(true);
		listChannels();
		while (isCrawling.get()) {
			Thread.sleep(TimeUnit.SECONDS.toMillis(1));
		}
		return channels;
	}

	/**
	 * Handle responses by the IRC server to the messages sent by the bot.
	 */
	@Override
	protected void onServerResponse(int code, String response) {
		switch (code) {
			case RPL_LISTSTART:
				break;
			case RPL_LIST:
				channels.add(response);
				break;
			case RPL_LISTEND:
				isCrawling.set(false);
				break;
			default:
				// TODO: Make this more robust and handle some error cases
				break;
		}
	}

}
