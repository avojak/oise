package com.avojak.webapp.oise.service.function;

import com.avojak.webapp.oise.model.ChannelListing;
import com.avojak.webapp.oise.service.runnable.ScrapingCallable;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Implementation of {@link Function} to add the scraped web content to channel listings.
 */
public class WebScrapingTransformFunction implements Function<List<ChannelListing>, List<ChannelListing>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(WebScrapingTransformFunction.class);

	private final ScrapingCallable.ScrapingCallableFactory scrapingCallableFactory;
	private final ListeningExecutorService executorService;

	private WebScrapingTransformFunction(final ScrapingCallable.ScrapingCallableFactory scrapingCallableFactory,
										 final ListeningExecutorService executorService) {
		this.scrapingCallableFactory = checkNotNull(scrapingCallableFactory, "scrapingCallableFactory cannot be null");
		this.executorService = checkNotNull(executorService, "executorService cannot be null");
	}

	@Nullable
	@Override
	public List<ChannelListing> apply(final List<ChannelListing> input) {
		// For each input listing, submit a callable to scrape the URLs
		final List<ListenableFuture<ChannelListing>> channelListingsFutures = new ArrayList<>();
		for (final ChannelListing channelListing : input) {
			channelListingsFutures.add(executorService.submit(scrapingCallableFactory.create(channelListing)));
		}
		final ListenableFuture<List<ChannelListing>> channelListingsFuture = Futures.successfulAsList(channelListingsFutures);

		// Try to get the results, but fall back on simply returning the original list in case of failure
		try {
			final List<ChannelListing> results = channelListingsFuture.get();
			// Remove nulls which are caused by a failure in one of the futures
			return Lists.newArrayList(results.stream().filter(Objects::nonNull).collect(Collectors.toList()));
		} catch (final InterruptedException | ExecutionException e) {
			LOGGER.error("Error while getting result of scraping", e);
			return input;
		}
	}

	/**
	 * Factory class to create instances of {@link WebScrapingTransformFunction} and inject beans.
	 */
	@Component
	public static class WebScrapingTransformFunctionFactory {

		@Autowired
		private ScrapingCallable.ScrapingCallableFactory scrapingCallableFactory;

		@Autowired
		@Qualifier("ScraperExecutorService")
		private ListeningExecutorService executorService;

		public WebScrapingTransformFunction create() {
			return new WebScrapingTransformFunction(scrapingCallableFactory, executorService);
		}

	}

}
