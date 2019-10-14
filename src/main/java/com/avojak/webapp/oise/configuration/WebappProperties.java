package com.avojak.webapp.oise.configuration;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Models the configurable application properties.
 */
public class WebappProperties {

	private final List<String> servers;
	private final String indexDirectory;

	WebappProperties(final List<String> servers, final String indexDirectory) {
		this.servers = checkNotNull(servers);
		this.indexDirectory = checkNotNull(indexDirectory);
	}

	public List<String> getServers() {
		return servers;
	}

	public String getIndexDirectory() {
		return indexDirectory;
	}

}
