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

	private final WebappProperties properties;
	private final String server;
	private final List<ChannelListing> channelListings;

	private IndexWriterRunnable(final WebappProperties properties, final String server,
								final List<ChannelListing> channelListings) {
		this.properties = checkNotNull(properties);
		this.server = checkNotNull(server);
		this.channelListings = checkNotNull(channelListings);
	}

	@Override
	public void run() {
		final IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
		config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
		final IndexWriter indexWriter;
		try {
			indexWriter = new IndexWriter(FSDirectory.open(new File(properties.getIndexDirectory()).toPath()), config);
		} catch (final IOException e) {
			LOGGER.error("Failed to create index writer", e);
			throw new RuntimeException(e);
		}

		for (final ChannelListing channelListing : channelListings) {
			final Document document = new Document();
			final String id = server + channelListing.getChannel();
			document.add(new StringField("id", id, TextField.Store.YES));
			document.add(new StoredField("server", server));
			document.add(new TextField("channel", channelListing.getChannel(), TextField.Store.YES));
			document.add(new TextField("topic", channelListing.getTopic(), TextField.Store.YES));
			document.add(new NumericDocValuesField("users", channelListing.getNumUsers()));
			try {
//				indexWriter.addDocument(document);
				indexWriter.updateDocument(new Term("id", id), document);
			} catch (final IOException e) {
				LOGGER.error("Failed to add document to index", e);
			}
		}

		try {
			indexWriter.commit();
			indexWriter.close();
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
		private WebappProperties properties;

		public IndexWriterRunnable create(final String server, final List<ChannelListing> channelListings) {
			return new IndexWriterRunnable(properties, server, channelListings);
		}

	}

}
