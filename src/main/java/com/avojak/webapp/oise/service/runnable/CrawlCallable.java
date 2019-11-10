package com.avojak.webapp.oise.service.runnable;

import com.avojak.webapp.oise.service.bot.CrawlerBot;

import java.util.List;
import java.util.concurrent.Callable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Implementation of {@link Callable} to crawl an IRC server for channels.
 */
public class CrawlCallable implements Callable<List<String>> {

	private final CrawlerBot bot;
	private final String server;

	public CrawlCallable(final CrawlerBot bot, final String server) {
		this.bot = checkNotNull(bot);
		this.server = checkNotNull(server);
	}

	@Override
	public List<String> call() throws Exception {
		return bot.crawl(server);
	}
}
