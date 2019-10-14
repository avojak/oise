package com.avojak.webapp.oise.configuration;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Models the configurable application properties.
 */
public class WebappProperties {

	private final List<String> servers;

	WebappProperties(final List<String> servers) {
		this.servers = checkNotNull(servers);
	}

	public List<String> getServers() {
		return servers;
	}
}
