import java.io.IOException;
import java.net.URL;
import java.nio.charset.MalformedInputException;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Crawls to web pages, parses out the HTML, and adds content into
 * inverted index.
 *
 * @author raywang
 *
 */
public class WebCrawler {

	private final ThreadSafeInvertedIndex index;
	private final WorkQueue minions;
	private static final Logger logger = LogManager.getLogger();
	private final ReadWriteLock lock;
	private final HashSet<String> links;

	public WebCrawler(int threadCount, ThreadSafeInvertedIndex index) {
		this.index = index;
		minions = new WorkQueue(threadCount);
		lock = new ReadWriteLock();
		links = new HashSet<>();
	}

	private void urlHelper(String url) {
		if (links.size() < 50 && !links.contains(url)) {
			links.add(url);
			minions.execute(new Minion(url));
		}
	}

	/**
	 * Private inner class that implements Runnable and overrides run()
	 *
	 * @author raywang
	 *
	 */
	private class Minion implements Runnable {

		private String link;

		public Minion(String link) {
			this.link = link;
			logger.debug("Minion created: {}", link);
		}

		@Override
		public void run() {

			try {

				String html = HTTPFetcher.fetchHTML(link);

				ArrayList<String> innerLinks =
						LinkParser.listLinks(new URL (link), html);
				logger.debug("innerLinks size {}", innerLinks.size());

				lock.lockReadWrite();
				for (int i = 0; i < innerLinks.size(); i++) {
					urlHelper(innerLinks.get(i));
				}
				lock.unlockReadWrite();

				html = HTMLCleaner.cleanHTML(html);
				String [] words = InvertedIndexBuilder.split(html);

				InvertedIndex local = new InvertedIndex();
				int position = 1;
				for (String word : words) {
					local.add(word, link, position);
					position++;
				}

				index.addAll(local);

				logger.debug("Minion finished {}", link);

			} catch (IOException e) {
				System.err.println("Link could not be added.");
				logger.debug("Invalid link");
			}
			logger.debug("Minion finished with {}", link);
		}
	}

	/**
	 * Creates a new thread to parse link.
	 *
	 * @param link
	 * @throws MalformedInputException
	 */
	public void traverse(String link) throws MalformedInputException {

		lock.lockReadWrite();
		urlHelper(link);
		lock.unlockReadWrite();
		finish();
		logger.debug("End WebCrawler traverse");

	}

	/**
	 * Helper method, that helps a thread wait until all of the current
	 * work is done. This is useful for resetting the counters or shutting
	 * down the work queue.
	 */
	public void finish() {
		minions.finish();
	}

	/**
	 * Will shutdown the work queue after all the current pending work is
	 * finished. Necessary to prevent our code from running forever in the
	 * background.
	 */
	public void shutdown() {
		finish();
		logger.debug("Shutting down");
		minions.shutdown();
	}

}
