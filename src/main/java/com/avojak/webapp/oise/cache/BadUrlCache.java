package com.avojak.webapp.oise.cache;

import com.linkedin.urls.Url;

import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public class BadUrlCache {

	final Set<Url> urls = new HashSet<>();

	public void insert(final Url url) {
		checkNotNull(url, "url cannot be null");
		urls.add(url);
	}

	public boolean contains(final Url url) {
		checkNotNull(url, "url cannot be null");
		return urls.contains(url);
	}

}