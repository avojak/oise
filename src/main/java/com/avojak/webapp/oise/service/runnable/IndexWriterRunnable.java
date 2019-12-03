package com.avojak.webapp.oise.service.runnable;

import com.avojak.webapp.oise.configuration.WebappProperties;
import com.avojak.webapp.oise.model.ChannelListing;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.soap.Text;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Implementation of {@link Runnable} to write channel listings to the index.
 */
public class IndexWriterRunnable implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(IndexWriterRunnable.class);

	private final IndexWriter indexWriter;
	private final String server;
	private final List<ChannelListing> channelListings;

	private IndexWriterRunnable(final IndexWriter indexWriter, final String server,
								final List<ChannelListing> channelListings) {
		this.indexWriter = checkNotNull(indexWriter);
		this.server = checkNotNull(server);
		this.channelListings = checkNotNull(channelListings);
	}

	@Override
	public void run() {
		for (final ChannelListing channelListing : channelListings) {
			final Document document = new Document();
			final String id = server + channelListing.getChannel();
			document.add(new StringField("id", id, TextField.Store.YES));
			document.add(new StoredField("server", server));
			document.add(new TextField("channel", channelListing.getChannel(), TextField.Store.YES));
			document.add(new TextField("topic", channelListing.getTopic(), TextField.Store.YES));
			document.add(new TextField("urlContent", channelListing.getUrlContent(), TextField.Store.YES));
			document.add(new NumericDocValuesField("users", channelListing.getNumUsers()));
			try {
				indexWriter.updateDocument(new Term("id", id), document);
			} catch (final IOException e) {
				LOGGER.error("Failed to add document to index", e);
			}
		}

		try {
			indexWriter.commit();
		} catch (final IOException e) {
			LOGGER.error("Failed to commit changes to the index", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Factory class to create instances of {@link IndexWriterRunnable} to inject the {@link IndexWriter} bean.
	 */
	@Component
	public static class IndexWriterRunnableFactory {

		@Autowired
		private IndexWriter indexWriter;

		public IndexWriterRunnable create(final String server, final List<ChannelListing> channelListings) {
			return new IndexWriterRunnable(indexWriter, server, channelListings);
		}

	}

}
