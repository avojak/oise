package com.avojak.webapp.oise.service.runnable;

import com.avojak.webapp.oise.cache.BadUrlCache;
import com.avojak.webapp.oise.model.ChannelListing;
import com.linkedin.urls.Url;
import com.linkedin.urls.detection.UrlDetector;
import com.linkedin.urls.detection.UrlDetectorOptions;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Implementation of {@link Callable} to scrape web content given a URL.
 */
public class ScrapingCallable implements Callable<ChannelListing> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScrapingCallable.class);

	private final ChannelListing originalListing;
	private final BadUrlCache badUrlCache;

	private ScrapingCallable(final ChannelListing originalListing, final BadUrlCache badUrlCache) {
		this.originalListing = checkNotNull(originalListing, "originalListing cannot be null");
		this.badUrlCache = checkNotNull(badUrlCache, "badUrlCache cannot be null");
	}

	@Override
	public ChannelListing call() {
		final UrlDetector detector = new UrlDetector(originalListing.getTopic(), UrlDetectorOptions.Default);
		final List<Url> urls = detector.detect();
		final StringBuilder listingUrlContentBuilder = new StringBuilder();
		for (final Url url : urls) {
			if (badUrlCache.contains(url)) {
				LOGGER.debug("Skipping known bad URL: " + url.toString());
				continue;
			}
			LOGGER.trace("Scraping URL: " + url.toString());
			try {
				final Document document = Jsoup.connect(url.toString()).get();
				final StringBuilder sb = new StringBuilder();
				sb.append(document.body().text());
				sb.append(document.title());
				// TODO: Maybe choose some specific header attributes to look for as well?
				listingUrlContentBuilder.append(sb.toString());
			} catch (final IOException e) {
				badUrlCache.insert(url);
			}
		}
		// Create a new listing with the original fields, but add the URL content
		final ChannelListing listing = new ChannelListing(originalListing.getServer(), originalListing.getChannel(),
				originalListing.getTopic(), originalListing.getNumUsers());
		listing.setUrlContent(listingUrlContentBuilder.toString());
		return listing;
	}

	/**
	 * Factory class to create instances of {@link ScrapingCallable} to inject the {@link BadUrlCache} bean.
	 */
	@Component
	public static class ScrapingCallableFactory {

		@Autowired
		private BadUrlCache badUrlCache;

		public ScrapingCallable create(final ChannelListing originalListing) {
			return new ScrapingCallable(originalListing, badUrlCache);
		}

	}

}
