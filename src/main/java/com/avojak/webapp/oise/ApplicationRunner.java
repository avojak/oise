package com.avojak.webapp.oise;

import com.avojak.webapp.oise.service.ServerCrawlerService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class ApplicationRunner implements CommandLineRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationRunner.class);

	@Autowired
	private ServerCrawlerService serverCrawlerService;

	@Override
	public void run(final String... args) throws Exception {
		Set<com.google.common.util.concurrent.Service> services = new HashSet<>();
		services.add(serverCrawlerService);

		final ServiceManager manager = new ServiceManager(services);
		manager.addListener(new ServiceManager.Listener() {
								public void stopped() {
									LOGGER.info("All services stopped");
								}

								public void healthy() {
									LOGGER.info("All services healthy");
								}

								public void failure(com.google.common.util.concurrent.Service service) {
									LOGGER.error("Service failure", service.failureCause());
									System.exit(1);
								}
							},
				MoreExecutors.directExecutor());
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				manager.stopAsync().awaitStopped(5, TimeUnit.SECONDS);
			} catch (final TimeoutException e) {
				LOGGER.warn("Timed out waiting for services to stop gracefully", e);
			}
		}));
		manager.startAsync();
	}
}
