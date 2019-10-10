package com.avojak.webapp.oise.service.runnable;

import com.linkedin.urls.Url;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.Callable;

import static com.google.common.base.Preconditions.checkNotNull;

public class WebScraperCallable implements Callable<String> {

	private final RestTemplate restTemplate;
	private final Url url;

	public WebScraperCallable(final RestTemplate restTemplate, final Url url) {
		this.restTemplate = checkNotNull(restTemplate);
		this.url = checkNotNull(url);
	}

	@Override
	public String call() throws Exception {
		try {
			final ResponseEntity<String> response = restTemplate.exchange(url.getFullUrl(), HttpMethod.POST, null, String.class);
			if (response.getStatusCode() != HttpStatus.OK) {
				return "";
			}
			final Document document = Jsoup.parse(response.getBody());
			return document.body().text();
		} catch (final HttpClientErrorException e) {
			// TODO: Log this
			return "";
		}
	}
}
