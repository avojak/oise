package com.avojak.webapp.oise.service.callback;

import com.avojak.webapp.oise.event.ServerCrawlCompleteEvent;
import com.avojak.webapp.oise.model.ChannelListing;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.FutureCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Implementation of {@link FutureCallback} to post results to the event bus upon successfully crawling a server for
 * channels.
 */
@SuppressWarnings("UnstableApiUsage")
public class ServerCrawlCallback implements FutureCallback<List<ChannelListing>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServerCrawlCallback.class);

	private final String server;
	private final EventBus eventBus;

	private ServerCrawlCallback(final String server, final EventBus eventBus) {
		this.server = checkNotNull(server);
		this.eventBus = checkNotNull(eventBus);
	}

	@Override
	public void onSuccess(final List<ChannelListing> result) {
		LOGGER.debug("Successfully crawled {} ({} channels)", server, result.size());
		eventBus.post(new ServerCrawlCompleteEvent(server, result));
	}

	@Override
	public void onFailure(final Throwable t) {
		LOGGER.error("Error while crawling server: " + server, t);
		// TODO: Handle this failure
	}

	/**
	 * Factory class to create instances of {@link ServerCrawlCallback} and inject the {@link EventBus} bean.
	 */
	@Component
	public static class ServerCrawlCallbackFactory {

		@Autowired
		private EventBus eventBus;

		public ServerCrawlCallback create(final String server) {
			return new ServerCrawlCallback(server, eventBus);
		}

	}

}
