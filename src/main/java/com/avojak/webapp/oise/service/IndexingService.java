package com.avojak.webapp.oise.service;

import com.avojak.webapp.oise.event.ServerCrawlCompleteEvent;
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

/**
 * Service to handle indexing of the crawler results.
 */
@SuppressWarnings("UnstableApiUsage")
@Service
public class IndexingService extends AbstractIdleService {

	private static final Logger LOGGER = LoggerFactory.getLogger(IndexingService.class);

	@Autowired
	@Qualifier("IndexerExecutorService")
	private ListeningExecutorService executorService;

	@Autowired
	private IndexWriter indexWriter;

	@Autowired
	private EventBus eventBus;

	@Autowired
	private IndexWriterRunnable.IndexWriterRunnableFactory indexWriterRunnableFactory;

	@Subscribe
	public void onServerCrawlComplete(final ServerCrawlCompleteEvent event) {
		LOGGER.debug("Received ServerCrawlCompleteEvent for server: {}", event.getServer());
		final ListenableFuture<?> future = executorService.submit(indexWriterRunnableFactory.create(event.getServer(), event.getChannelListings()));
		Futures.addCallback(future, new FutureCallback<Object>() {
			@Override
			public void onSuccess(final Object result) {
				LOGGER.info("Successfully indexed channel listings for server: {}", event.getServer());
			}

			@Override
			public void onFailure(final Throwable t) {
				LOGGER.error("Failed to index channel listings for server: " + event.getServer(), t);
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
		indexWriter.close();
		executorService.shutdownNow();
	}

	@Override
	protected String serviceName() {
		return getClass().getSimpleName();
	}

}
