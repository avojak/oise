package com.avojak.webapp.oise.service;

import com.avojak.webapp.oise.configuration.WebappProperties;
import com.avojak.webapp.oise.model.ChannelListing;
import com.avojak.webapp.oise.service.bot.CrawlerBot;
import com.avojak.webapp.oise.service.callable.CrawlCallable;
import com.avojak.webapp.oise.service.callback.ServerCrawlCallback;
import com.avojak.webapp.oise.service.callback.CrawlerBotCallback;
import com.avojak.webapp.oise.service.function.ChannelListingTransformFunction;
import com.google.common.util.concurrent.AbstractScheduledService;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class CrawlingService extends AbstractScheduledService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CrawlingService.class);

	@Autowired
	@Qualifier("CrawlerExecutorService")
	private ListeningExecutorService executorService;

	@Autowired
	private ServerCrawlCallback.ServerCrawlCallbackFactory serverCrawlCallbackFactory;

	@Autowired
	private WebappProperties properties;

	@Override
	protected void runOneIteration() throws Exception {
		LOGGER.info("Preparing to crawl {} servers", properties.getServers().size());
		final List<ListenableFuture<List<ChannelListing>>> channelListingsFutures = new ArrayList<>();
		for (final String server : properties.getServers()) {
			// Create the bot that will crawl the server
			final CrawlerBot bot = new CrawlerBot();
			bot.setAutoNickChange(true);
			bot.setVerbose(false);

			// Perform the crawl of the server
			final ListenableFuture<List<String>> future = executorService.submit(new CrawlCallable(bot, server));

			// Cleanup the bot after crawling the server
			Futures.addCallback(future, new CrawlerBotCallback(bot, server), executorService);

			// Convert the messages from the IRC server into models we can process
			final ListenableFuture<List<ChannelListing>> channelListingsFuture = Futures.transform(future, new ChannelListingTransformFunction(server), executorService);

			// Post the results to the event bus
			Futures.addCallback(channelListingsFuture, serverCrawlCallbackFactory.create(server), executorService);

			// Accumulate all the futures
			channelListingsFutures.add(channelListingsFuture);
		}

		// Consolidate all futures into one future from which we can report overall success/failure
		final ListenableFuture<List<List<ChannelListing>>> crawlFuture = Futures.successfulAsList(channelListingsFutures);
		Futures.addCallback(crawlFuture, new FutureCallback<List<List<ChannelListing>>>() {
			@Override
			public void onSuccess(final List<List<ChannelListing>> result) {
				final int numServersCrawled = result.size();
				int numChannels = 0;
				for (final List<ChannelListing> serverChannelListings : result) {
					numChannels += serverChannelListings.size();
				}
				LOGGER.info("Successfully crawled {} servers for a total of {} channels", numServersCrawled, numChannels);
			}

			@Override
			public void onFailure(final Throwable t) {
				LOGGER.error("Error while crawling servers", t);
			}
		}, executorService);

		// TODO: Should probably time this out
		crawlFuture.get();
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
		return Scheduler.newFixedRateSchedule(Duration.ZERO, Duration.ofHours(24));
	}
}
