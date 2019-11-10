package com.avojak.webapp.oise.service.callback;

import com.avojak.webapp.oise.service.bot.CrawlerBot;
import com.google.common.util.concurrent.FutureCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Implementation of {@link FutureCallback} to cleanup the {@link CrawlerBot} upon successful or failed execution.
 */
public class CrawlerBotCallback implements FutureCallback<List<String>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CrawlerBotCallback.class);

	private final CrawlerBot bot;
	private final String server;

	public CrawlerBotCallback(final CrawlerBot bot, final String server) {
		this.bot = checkNotNull(bot);
		this.server = checkNotNull(server);
	}

	@Override
	public void onSuccess(List<String> result) {
		disposeBot();
	}

	@Override
	public void onFailure(final Throwable t) {
		disposeBot();
	}

	private void disposeBot() {
		bot.disconnect();
		LOGGER.trace("Disconnected bot {} from {}", bot.hashCode(), server);
		try {
			bot.dispose();
		} catch (final Exception e) {
			LOGGER.error("Error while disposing bot");
		}
		LOGGER.trace("Disposed bot {}", bot.hashCode());
	}

}
