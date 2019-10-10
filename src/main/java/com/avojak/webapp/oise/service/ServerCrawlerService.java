package com.avojak.webapp.oise.service;

import com.avojak.webapp.oise.configuration.WebappProperties;
import com.avojak.webapp.oise.model.ServerCrawlerResult;
import com.avojak.webapp.oise.service.runnable.ServerCrawlerCallable;
import com.google.common.util.concurrent.AbstractScheduledService;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ServerCrawlerService extends AbstractScheduledService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServerCrawlerService.class);

	@Autowired
	private ListeningExecutorService executorService;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private WebappProperties properties;

	@Override
	protected void runOneIteration() throws Exception {
		LOGGER.debug("Preparing to crawl {} servers", properties.getServers().size());
		final List<ListenableFuture<ServerCrawlerResult>> futures = new ArrayList<>();
		for (final String server : properties.getServers()) {
			final ListenableFuture<ServerCrawlerResult> future = executorService.submit(new ServerCrawlerCallable(server, executorService, restTemplate));
			Futures.addCallback(future, new FutureCallback<ServerCrawlerResult>() {
				@Override
				public void onSuccess(final ServerCrawlerResult result) {
					LOGGER.debug("Successfully crawled {} [{} channels]", server, result.getChannelResults().size());
				}

				@Override
				public void onFailure(final Throwable t) {
					LOGGER.warn("Failed to crawl server: " + server, t);
				}
			}, executorService);
			futures.add(future);
		}
		final ListenableFuture<List<ServerCrawlerResult>> future = Futures.successfulAsList(futures);
		final List<ServerCrawlerResult> results = future.get(60, TimeUnit.SECONDS);

		int totalChannels = 0;
		for (final ServerCrawlerResult result : results) {
			totalChannels += result.getChannelResults().size();
		}
		LOGGER.info("Crawled {} channels on {} servers", totalChannels, properties.getServers().size());
	}

	@Override
	protected void shutDown() {
		executorService.shutdownNow();
	}

	@Override
	protected String serviceName() {
		return getClass().getSimpleName();
	}

	@Override
	protected Scheduler scheduler() {
		return Scheduler.newFixedRateSchedule(Duration.ZERO, Duration.ofHours(1));
	}
}
