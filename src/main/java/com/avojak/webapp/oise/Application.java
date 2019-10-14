package com.avojak.webapp.oise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * The main application class.
 */
@SpringBootApplication
@EnableScheduling
public class Application {

	public static void main(String... args) {
		SpringApplication.run(Application.class, args);
	}

}
