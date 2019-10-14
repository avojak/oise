package com.avojak.webapp.oise.configuration;

import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Handles the application configuration.
 */
@Configuration
public class WebappConfiguration {

	@Value("${oise.serversfile}")
	private String serversFile;

	@Bean
	public WebappProperties webappProperties() {
		final List<String> servers = new ArrayList<>();
		try (final BufferedReader reader = new BufferedReader(new FileReader(serversFile))) {
			String line = reader.readLine();
			while (line != null) {
				final String server = line.trim();
				if (!server.isEmpty()) {
					servers.add(server);
				}
				line = reader.readLine();
			}
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
		return new WebappProperties(servers);
	}

	@Bean(name = "CrawlerExecutorService")
	public ListeningExecutorService crawlerExecutorService() {
		final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("crawler-%d").build();
		return MoreExecutors.listeningDecorator(Executors.newCachedThreadPool(threadFactory));
	}

	@Bean(name = "IndexerExecutorService")
	public ListeningExecutorService indexerExecutorService() {
		final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("indexer-%d").build();
		return MoreExecutors.listeningDecorator(Executors.newCachedThreadPool(threadFactory));
	}

	@Bean
	public EventBus eventBus() {
		return new EventBus();
	}

	@Bean
	public RestTemplate restTemplate() {
		int timeout = 5000; // Timeout in milliseconds
		HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
		clientHttpRequestFactory.setConnectTimeout(timeout);
		return new RestTemplate();
	}

}
