package com.avojak.webapp.oise.service.listener;

import com.google.common.util.concurrent.ServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link ServiceManager.Listener} to report on the state transitions of the service manager.
 */
public class ServiceManagerStateListener extends ServiceManager.Listener {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceManagerStateListener.class);

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

}
