package com.avojak.webapp.oise.service;

import com.avojak.webapp.oise.event.ServerCrawlCompleteEvent;
import com.avojak.webapp.oise.model.ChannelListing;
import com.avojak.webapp.oise.service.runnable.IndexWriterRunnable;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import org.apache.lucene.index.IndexWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

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

	@Autowired
	private IndexWriterRunnable.IndexWriterRunnableFactory indexWriterRunnableFactory;

	@Subscribe
	public void onServerCrawlComplete(final ServerCrawlCompleteEvent event) {
		LOGGER.debug("Received ServerCrawlCompleteEvent for server: {}", event.getServer());
		addChannelsToIndex(event.getServer(), event.getChannelListings());
	}

	private void addChannelsToIndex(final String server, final List<ChannelListing> channelListings) {
		final ListenableFuture<?> future = executorService.submit(indexWriterRunnableFactory.create(server, channelListings));
		Futures.addCallback(future, new FutureCallback<Object>() {
			@Override
			public void onSuccess(final Object result) {
				LOGGER.info("Successfully indexed channel listings for server: {}", server);
			}

			@Override
			public void onFailure(final Throwable t) {
				LOGGER.error("Failed to index channel listings for server: " + server, t);
			}
		}, executorService);
	}

	@Override
	protected void startUp() {
		eventBus.register(this);
		LOGGER.debug("Registered {} to receive events", serviceName());
	}

	@Override
	protected void shutDown() throws IOException {
		executorService.shutdownNow();
	}

	@Override
	protected String serviceName() {
		return getClass().getSimpleName();
	}

}
