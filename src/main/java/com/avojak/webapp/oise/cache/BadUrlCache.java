package com.avojak.webapp.oise.cache;

import com.linkedin.urls.Url;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Models a cache of URLs which are found to be invalid and should not be scraped.
 */
@Component
public class BadUrlCache {

	private final Set<Url> urls = new HashSet<>();

	/**
	 * Insert a new URL into the cache.
	 *
	 * @param url
	 * 		The URL.
	 */
	public void insert(final Url url) {
		checkNotNull(url, "url cannot be null");
		urls.add(url);
	}

	/**
	 * Returns whether or not the given URL is contained in the cache.
	 *
	 * @param url
	 * 		The URL.
	 *
	 * @return {@code true} if the URL is present, otherwise {@code false}.
	 */
	public boolean contains(final Url url) {
		checkNotNull(url, "url cannot be null");
		return urls.contains(url);
	}

}