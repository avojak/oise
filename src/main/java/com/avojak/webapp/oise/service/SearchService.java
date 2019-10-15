package com.avojak.webapp.oise.service;

import com.avojak.webapp.oise.model.SearchResult;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class SearchService {

	@Autowired
	private IndexSearcher indexSearcher;

	public List<SearchResult> search(final String rawQuery) throws ParseException, IOException {
		final Analyzer analyzer = new StandardAnalyzer();
		final QueryParser queryParser = new MultiFieldQueryParser(new String[]{ "channel", "topic" }, analyzer);
		final Query query = queryParser.parse(rawQuery);
		final ScoreDoc[] scoreDocs = indexSearcher.search(query, 10, Sort.RELEVANCE, true).scoreDocs;
		final List<SearchResult> searchResults = new ArrayList<>();
		for (final ScoreDoc scoreDoc : scoreDocs) {
			final Document document = indexSearcher.doc(scoreDoc.doc);
			final String server = document.get("server");
			final String channel = document.get("channel");
			final String topic = document.get("topic");
			// TODO: Why is this null...?
//			final int users = document.getField("users").numericValue().intValue();
			searchResults.add(new SearchResult(server, channel, topic, 0, scoreDoc));
		}
		return searchResults;
	}

}
