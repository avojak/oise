package com.avojak.webapp.oise.controller;

import com.avojak.webapp.oise.model.SearchResult;
import com.avojak.webapp.oise.service.SearchService;
import org.apache.lucene.queryparser.classic.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * The REST controller for the search endpoints.
 */
@RestController
public class SearchController {

	@Autowired
	private SearchService searchService;

	/**
	 * Defines the REST endpoint for searching.
	 * @param query The search query.
	 * @return The list of {@link SearchResult} objects.
	 */
	@GetMapping(value = "/api/v1/search")
	public List<SearchResult> searchV1(@RequestParam("q") String query) {
		try {
			return searchService.search(query);
		} catch (final ParseException | IOException e) {
			throw new RuntimeException(e);
		}
	}

}
