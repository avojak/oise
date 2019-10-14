package com.avojak.webapp.oise;

import com.avojak.webapp.oise.service.CrawlingService;
import com.avojak.webapp.oise.service.IndexingService;
import com.avojak.webapp.oise.service.listener.ServiceManagerStateListener;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * The main runner to start the underlying services.
 */
@org.springframework.stereotype.Service
public class ApplicationRunner implements CommandLineRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationRunner.class);

	@Autowired
	private CrawlingService crawlingService;

	@Autowired
	private IndexingService indexingService;

	@Override
	public void run(final String... args) throws Exception {
		Set<Service> services = new HashSet<>();
		services.add(crawlingService);
		services.add(indexingService);

		final ServiceManager manager = new ServiceManager(services);
		manager.addListener(new ServiceManagerStateListener(), MoreExecutors.directExecutor());
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				manager.stopAsync().awaitStopped(5, TimeUnit.SECONDS);
			} catch (final TimeoutException e) {
				LOGGER.warn("Timed out waiting for services to stop gracefully", e);
			}
		}));

		manager.startAsync();
		manager.awaitHealthy(30, TimeUnit.SECONDS);
	}
}
