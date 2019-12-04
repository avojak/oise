package com.avojak.webapp.oise.service;

import com.avojak.webapp.oise.configuration.WebappProperties;
import com.avojak.webapp.oise.model.ChannelListing;
import com.avojak.webapp.oise.service.bot.CrawlerBot;
import com.avojak.webapp.oise.service.function.WebScrapingTransformFunction;
import com.avojak.webapp.oise.service.runnable.CrawlCallable;
import com.avojak.webapp.oise.service.callback.ServerCrawlCallback;
import com.avojak.webapp.oise.service.callback.CrawlerBotCallback;
import com.avojak.webapp.oise.service.function.ChannelListingTransformFunction;
import com.google.common.util.concurrent.AbstractScheduledService;
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

/**
 * Service to crawl IRC servers for channels.
 */
@Service
public class CrawlingService extends AbstractScheduledService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CrawlingService.class);

	@Autowired
	@Qualifier("CrawlerExecutorService")
	private ListeningExecutorService executorService;

	@Autowired
	private ServerCrawlCallback.ServerCrawlCallbackFactory serverCrawlCallbackFactory;

	@Autowired
	private WebScrapingTransformFunction.WebScrapingTransformFunctionFactory webScrapingTransformFunctionFactory;

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

			// Scrape the URLs in the topic and update the listing objects
			final ListenableFuture<List<ChannelListing>> scrapedListingsFuture = Futures.transform(channelListingsFuture, webScrapingTransformFunctionFactory.create(), executorService);

			// Post the results to the event bus
			Futures.addCallback(scrapedListingsFuture, serverCrawlCallbackFactory.create(server), executorService);

			// Accumulate all the futures
			channelListingsFutures.add(scrapedListingsFuture);
		}

		// Consolidate all futures into one future from which we can report overall success/failure
		final ListenableFuture<List<List<ChannelListing>>> crawlFuture = Futures.successfulAsList(channelListingsFutures);

		// TODO: Should probably time this out
		final List<List<ChannelListing>> results = crawlFuture.get();
		final int totalServers = results.size();
		int numServersSuccessfullyCrawled = 0;
		int numChannels = 0;
		for (final List<ChannelListing> serverChannelListings : results) {
			// Ignore null entries for failed futures
			if (serverChannelListings == null) {
				continue;
			}
			numChannels += serverChannelListings.size();
			numServersSuccessfullyCrawled++;
		}
		LOGGER.info("Successfully crawled {}/{} servers for a total of {} channels", numServersSuccessfullyCrawled, totalServers, numChannels);
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
