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
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Service to crawl IRC servers for channels.
 */
@SuppressWarnings("UnstableApiUsage")
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
	protected void runOneIteration() {
		final int totalServers = properties.getServers().size();
		LOGGER.info("Preparing to crawl {} servers", totalServers);

		final AtomicInteger crawlProgress = new AtomicInteger(0);
		final AtomicInteger numServersSuccessfullyCrawled = new AtomicInteger(0);
		final AtomicInteger numChannelsFound = new AtomicInteger(0);

		final List<String> servers = new ArrayList<>(properties.getServers());
		final AtomicReference<List<String>> crawlQueue = new AtomicReference<>(servers);

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
			Futures.addCallback(scrapedListingsFuture, new FutureCallback<List<ChannelListing>>() {
				@Override
				public void onSuccess(final List<ChannelListing> result) {
					numServersSuccessfullyCrawled.incrementAndGet();
					numChannelsFound.addAndGet(result.size());
					updateProgress();
				}

				@Override
				public void onFailure(final Throwable t) {
					updateProgress();
				}

				private void updateProgress() {
					final int progress = crawlProgress.incrementAndGet();
					LOGGER.info("Crawl progress: {}/{}", progress, totalServers);
					LOGGER.debug("Remaining servers: {}", StringUtils.collectionToDelimitedString(crawlQueue.updateAndGet(strings -> {
						strings.remove(server);
						return strings;
					}), ", "));
				}
			}, executorService);

			// Accumulate all the futures
			channelListingsFutures.add(scrapedListingsFuture);
		}

		// Consolidate all futures into one future from which we can report overall success/failure
		final ListenableFuture<List<List<ChannelListing>>> crawlFuture = Futures.successfulAsList(channelListingsFutures);

		try {
			crawlFuture.get(1, TimeUnit.HOURS);
		} catch (final InterruptedException | ExecutionException | TimeoutException e) {
			LOGGER.error("Timed out waiting for crawl to complete", e);
			LOGGER.debug("Un-crawled servers: {}", StringUtils.collectionToDelimitedString(crawlQueue.get(), ", "));
		}
		LOGGER.info("Successfully crawled {}/{} servers for a total of {} channels", numServersSuccessfullyCrawled.get(), totalServers, numChannelsFound.get());
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
