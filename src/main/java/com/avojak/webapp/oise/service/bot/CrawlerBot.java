package com.avojak.webapp.oise.service.bot;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.PircBot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implementation of {@link PircBot} to crawl a given server for the channels it contains.
 */
public class CrawlerBot extends PircBot {

	final AtomicBoolean isCrawling = new AtomicBoolean(false);
	final List<String> channels = new ArrayList<>();

	public List<String> crawl(final String server) throws IOException, IrcException, InterruptedException {
		connect(server);
		isCrawling.set(true);
		listChannels();
		while (isCrawling.get()) {
			Thread.sleep(TimeUnit.SECONDS.toMillis(1));
		}
		return channels;
	}

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
				break;
		}
	}

}
