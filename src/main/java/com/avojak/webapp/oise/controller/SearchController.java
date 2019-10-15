package com.avojak.webapp.oise.controller;

import com.avojak.webapp.oise.model.SearchResult;
import com.avojak.webapp.oise.service.SearchService;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
public class SearchController {

	@Autowired
	private SearchService searchService;

	@GetMapping(value = "/hello")
	public List<SearchResult> hello() {
		try {
			return searchService.search("lego");
		} catch (final ParseException | IOException e) {
			throw new RuntimeException(e);
		}
	}

}
