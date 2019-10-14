package com.avojak.webapp.oise.service;

import com.avojak.webapp.oise.event.ServerCrawlCompleteEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.common.util.concurrent.ListeningExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Service to handle indexing of the crawler results.
 */
@Service
public class IndexingService extends AbstractIdleService {

	private static final Logger LOGGER = LoggerFactory.getLogger(IndexingService.class);

	@Autowired
	@Qualifier("IndexerExecutorService")
	private ListeningExecutorService executorService;

	@Autowired
	private EventBus eventBus;

	@Subscribe
	public void onServerCrawlComplete(final ServerCrawlCompleteEvent event) {
		LOGGER.debug("Received ServerCrawlCompleteEvent for server: {}", event.getServer());
	}

	@Override
	protected void startUp() throws Exception {
		LOGGER.debug("Registering {} to receive events", serviceName());
		eventBus.register(this);
	}

	@Override
	protected void shutDown() throws Exception {
		executorService.shutdownNow();
	}

	@Override
	protected String serviceName() {
		return getClass().getSimpleName();
	}

}
