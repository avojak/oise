package com.avojak.webapp.oise.controller;

import com.avojak.webapp.oise.model.SearchResult;
import com.avojak.webapp.oise.service.SearchService;
import org.apache.lucene.queryparser.classic.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The REST controller for the UI endpoints.
 */
@Controller
@RequestMapping("/")
public class ViewController {

	@Autowired
	private SearchService searchService;

	@GetMapping("")
	public String index(final Model model) {
		model.addAttribute("title", "OISE | Open IRC Search Engine");
		return "search";
	}

	@GetMapping("results")
	public String searchResults(@RequestParam("q") String query, final Model model) {
		// Send empty queries back to the search page
		if (query == null || query.trim().isEmpty()) {
			return "redirect:/";
		}
		final List<SearchResult> results = new ArrayList<>();
		try {
			results.addAll(searchService.search(query));
		} catch (final ParseException | IOException e) {
			// TODO: Handle this
			e.printStackTrace();
		}
		model.addAttribute("title", "OISE | Open IRC Search Engine");
		model.addAttribute("query", query);
		model.addAttribute("results", results);
		return "results";
	}

}
