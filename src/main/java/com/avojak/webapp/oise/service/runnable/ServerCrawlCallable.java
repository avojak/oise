package com.avojak.webapp.oise.service.runnable;

import com.avojak.webapp.oise.model.ChannelTopicResult;
import com.avojak.webapp.oise.model.ServerCrawlResult;
import com.avojak.webapp.oise.service.CrawlerBot;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

public class ServerCrawlCallable implements Callable<ServerCrawlResult> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServerCrawlCallable.class);

	private final String server;
	private final ListeningExecutorService executorService;

	public ServerCrawlCallable(final String server, final ListeningExecutorService executorService) {
		this.server = checkNotNull(server);
		this.executorService = checkNotNull(executorService);
	}

	@Override
	public ServerCrawlResult call() throws Exception {
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
		final ListenableFuture<ServerCrawlResult> result = Futures.transform(future, (messages) -> {
			if (messages == null) {
				return new ServerCrawlResult(server, new ArrayList<>());
			}
			final List<ChannelTopicResult> channelTopicResults = new ArrayList<>();
			for (final String message : messages) {
				final String trimmed = message.replace("PircBot", "").trim();
				final String channel = trimmed.split(" ")[0];
				final int numUsers = Integer.parseInt(trimmed.split(" ")[1]);
				final String topic = trimmed.substring(trimmed.indexOf(":") + 1);
				System.out.println("Channel: " + channel + ", numUsers: " + numUsers + ", topic: " + topic);

				channelTopicResults.add(new ChannelTopicResult(channel, numUsers, topic));
			}
			return new ServerCrawlResult(server, channelTopicResults);
		}, executorService);

		return result.get(60, TimeUnit.SECONDS);
	}
}
