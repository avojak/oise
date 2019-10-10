package com.avojak.webapp.oise.service.runnable;

import com.avojak.webapp.oise.model.ChannelResult;
import com.avojak.webapp.oise.model.ServerCrawlerResult;
import com.avojak.webapp.oise.service.CrawlerBot;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.linkedin.urls.Url;
import com.linkedin.urls.detection.UrlDetector;
import com.linkedin.urls.detection.UrlDetectorOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.google.common.base.Preconditions.checkNotNull;

public class ServerCrawlerCallable implements Callable<ServerCrawlerResult> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServerCrawlerCallable.class);

	private final String server;
	private final ListeningExecutorService executorService;
	private final RestTemplate restTemplate;

	public ServerCrawlerCallable(final String server, final ListeningExecutorService executorService, final
	RestTemplate restTemplate) {
		this.server = checkNotNull(server);
		this.executorService = checkNotNull(executorService);
		this.restTemplate = checkNotNull(restTemplate);
	}

	@Override
	public ServerCrawlerResult call() throws Exception {
		final CrawlerBot bot = new CrawlerBot();
		bot.setVerbose(false);
		LOGGER.trace("Created bot {} for server {}", bot.hashCode(), server);
		final ListenableFuture<List<String>> future = executorService.submit(() -> bot.crawl(server));
		Futures.addCallback(future, new FutureCallback<List<String>>() {
			@Override
			public void onSuccess(List<String> result) {
				bot.disconnect();
				LOGGER.trace("Disconnected bot {} from {}", bot.hashCode(), server);
				bot.dispose();
				LOGGER.trace("Disposed bot {}", bot.hashCode());
			}

			@Override
			public void onFailure(final Throwable t) {
				bot.disconnect();
				LOGGER.trace("Disconnected bot {} from {}", bot.hashCode(), server);
				bot.dispose();
				LOGGER.trace("Disposed bot {}", bot.hashCode());
			}
		}, executorService);
//		final ListenableFuture<Map<String, String>> caught = Futures.catching(future, Exception.class, x -> new HashMap<>(), executorService);
		final ListenableFuture<ServerCrawlerResult> serverCrawlerResult = Futures.transform(future, (messages) -> {
			if (messages == null) {
				return new ServerCrawlerResult(server, new ArrayList<>());
			}
			final List<ChannelResult> channelResults = new ArrayList<>();
			for (final String message : messages) {
				final String trimmed = message.replace("PircBot", "").trim();
				final String channel = trimmed.split(" ")[0];
				final int numUsers = Integer.parseInt(trimmed.split(" ")[1]);
				final String topic = trimmed.substring(trimmed.indexOf(":") + 1);
//				System.out.println("Channel: " + channel + ", numUsers: " + numUsers + ", topic: " + topic);

				// Check if the topic contains any URLs
				final UrlDetector parser = new UrlDetector(topic, UrlDetectorOptions.Default);
				final List<Url> urls = parser.detect();

				// For each URL in the topic, scrape the web page
				final List<ListenableFuture<String>> urlFutures = new ArrayList<>();
				for (final Url url : urls) {
					urlFutures.add(executorService.submit(new WebScraperCallable(restTemplate, url)));
				}
				final ListenableFuture<List<String>> urlFuture = Futures.successfulAsList(urlFutures);
				final ListenableFuture<String> webContentFuture = Futures.transform(urlFuture, (contents) -> {
					final StringBuilder builder = new StringBuilder();
					for (final String content : contents) {
						builder.append(content);
					}
					return builder.toString();
				}, executorService);
				String webContent = "";
				try {
					webContent = webContentFuture.get(10, TimeUnit.SECONDS);
				} catch (InterruptedException | ExecutionException | TimeoutException e) {
					LOGGER.warn("Timed out waiting for web scraping to complete", e);
				}

				final ChannelResult result = new ChannelResult(channel, numUsers, topic, webContent);
				System.out.println(result);
				channelResults.add(result);
			}
			return new ServerCrawlerResult(server, channelResults);
		}, executorService);

		return serverCrawlerResult.get(60, TimeUnit.SECONDS);
	}
}
