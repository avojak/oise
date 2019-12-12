package com.avojak.webapp.oise.service;

import com.avojak.webapp.oise.configuration.WebappProperties;
import com.avojak.webapp.oise.model.SearchResult;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Service to search the index.
 */
@Service
public class SearchService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SearchService.class);

	@Autowired
	private WebappProperties properties;

	public List<SearchResult> search(final String rawQuery) throws ParseException, IOException {
		checkNotNull(rawQuery, "rawQuery cannot be null");
		final IndexSearcher indexSearcher;
		try {
			final Directory indexDirectory = FSDirectory.open(new File(properties.getIndexDirectory()).toPath());
			indexSearcher = new IndexSearcher(DirectoryReader.open(indexDirectory));
		} catch (final IOException e) {
			LOGGER.error("Error opening the index - does it exist yet?", e);
			return new ArrayList<>();
		}

		final Analyzer analyzer = new StandardAnalyzer();
		// Only the channel, topic, and urlContent fields contain textual data relevant to the search
		final QueryParser queryParser = new MultiFieldQueryParser(new String[]{ "channel", "topic", "urlContent" }, analyzer);
		final Query query = queryParser.parse(rawQuery);
		final ScoreDoc[] scoreDocs = indexSearcher.search(query, 10, Sort.RELEVANCE, true).scoreDocs;
		final List<SearchResult> searchResults = new ArrayList<>();
		for (final ScoreDoc scoreDoc : scoreDocs) {
			final Document document = indexSearcher.doc(scoreDoc.doc);
			final String server = document.get("server");
			final String channel = document.get("channel");
			final String topic = document.get("topic");
			final String urlContent = document.get("urlContent");
			final int users = Integer.parseInt(document.get("users"));
			searchResults.add(new SearchResult(server, channel, topic, urlContent, users, scoreDoc));
		}
		return searchResults;
	}

}
