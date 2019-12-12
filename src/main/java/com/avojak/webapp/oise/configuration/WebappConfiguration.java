package com.avojak.webapp.oise.configuration;

import com.avojak.webapp.oise.cache.BadUrlCache;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Handles the application configuration.
 */
@SuppressWarnings("UnstableApiUsage")
@Configuration
public class WebappConfiguration {

	@Value("${oise.serversfile}")
	private String serversFile;

	@Value("${oise.index.directory}")
	private String indexDirectory;

	@Value("${oise.crawler.max.threads}")
	private int maxCrawlerThreads;

	@Value("${oise.scraper.max.threads}")
	private int maxScraperThreads;

	/**
	 * Creates the WebappProperties bean.
	 * @return The properties.
	 */
	@Bean
	public WebappProperties webappProperties() {
		final List<String> servers = new ArrayList<>();
		try (final BufferedReader reader = new BufferedReader(new FileReader(Paths.get(serversFile).toFile()))) {
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

		return new WebappProperties(servers, Paths.get(indexDirectory).toString());
	}

	/**
	 * Creates the executor service for use by the {@link com.avojak.webapp.oise.service.CrawlingService}.
	 * @return the {@link ListeningExecutorService}.
	 */
	@Bean(name = "CrawlerExecutorService")
	public ListeningExecutorService crawlerExecutorService() {
		final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("crawler-%d").build();
		return MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(maxCrawlerThreads, threadFactory));
	}

	/**
	 * Creates the executor service for use by the {@link com.avojak.webapp.oise.service.IndexingService}.
	 * @return the {@link ListeningExecutorService}.
	 */
	@Bean(name = "IndexerExecutorService")
	public ListeningExecutorService indexerExecutorService() {
		// This has to be a single thread because the IndexWriter creates a lock on the index
		return MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());
	}

	/**
	 * Creates the event bus bean for communication across services.
	 * @return The {@link EventBus}.
	 */
	@Bean
	public EventBus eventBus() {
		return new EventBus();
	}

	/**
	 * Create and configure the REST client.
	 * @return The {@link RestTemplate}.
	 */
	@Bean
	public RestTemplate restTemplate() {
		int timeout = 5000; // Timeout in milliseconds
		HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
		clientHttpRequestFactory.setConnectTimeout(timeout);
		return new RestTemplate();
	}

	/**
	 * Create and configure the index writer.
	 * @return The {@link IndexWriter}.
	 */
	@Bean
	public IndexWriter indexWriter(final WebappProperties properties) throws Exception {
		final IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
		config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
		return new IndexWriter(FSDirectory.open(new File(properties.getIndexDirectory()).toPath()), config);
	}

}
