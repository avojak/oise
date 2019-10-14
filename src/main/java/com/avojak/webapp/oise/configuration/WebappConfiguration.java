package com.avojak.webapp.oise.configuration;

import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexDeletionPolicy;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.KeepOnlyLastCommitDeletionPolicy;
import org.apache.lucene.store.Directory;
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
import java.nio.file.Path;
import java.nio.file.Paths;
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

	@Value("${oise.index.directory}")
	private String indexDirectory;

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

	@Bean
	public IndexWriter indexWriter() throws IOException {
		// TODO: Update this configuration so that we append to an index if one already exists
		final Directory directory = FSDirectory.open(new File(indexDirectory).toPath());
		final IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
		return new IndexWriter(directory, config);
	}

}
