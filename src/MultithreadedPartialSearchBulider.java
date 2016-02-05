import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Stores partial search data structure with necessary methods to run partial
 * search using multithreading.
 *
 * @author raywang
 *
 */
public class MultithreadedPartialSearchBulider extends PartialSearchAbstract {

	/* Logger to help debug */
	private static final Logger logger = LogManager.getLogger();

	/* Work Queue for threads */
	private final WorkQueue minions;

	/* Stores query line and list of Search Results */
	private final Map<String, List<SearchResult>> resultMap;

	/* Stores word and relative path and position */
	private final ThreadSafeInvertedIndex index;

	/* Initializes WorkQueue and sets pending to 0 */
	public MultithreadedPartialSearchBulider(int threadCount,
			ThreadSafeInvertedIndex index) {
		resultMap = new LinkedHashMap<>();
		this.index = index;
		minions = new WorkQueue(threadCount);
	}

	/**
	 * Helper method, that helps a thread wait until all of the current work is
	 * done. This is useful for resetting the counters or shutting down the work
	 * queue.
	 */
	public synchronized void finish() {
		minions.finish();
	}

	/**
	 * Will shutdown the work queue after all the current pending work is
	 * finished. Necessary to prevent our code from running forever in the
	 * background.
	 */
	public synchronized void shutdown() {
		logger.debug("Shutting down");
		minions.finish();
		minions.shutdown();
	}

	/**
	 * Private inner class that implements Runnable and overrides run().
	 *
	 * @author raywang
	 *
	 */
	private class Minion implements Runnable {

		private String line;

		public Minion(String line) {
			this.line = line;
			logger.debug("New minion created.");
		}

		@Override
		public void run() {
			try {
				logger.debug("Running partial search minion. {}", line);
				String [] queries = InvertedIndexBuilder.split(line);
				List<SearchResult> resultsList = index.partialSearch(queries);
				synchronized (resultMap) {
					resultMap.put(line, resultsList);
				}
			} finally {
				logger.debug("Partial search minion finished. {}", line);
			}
		}
	}

	@Override
	public void parseLine(String line) {
		synchronized (resultMap) {
			resultMap.put(line, null);
		}
		minions.execute(new Minion(line));
	}

	/**
	 * writes entire partial search
	 * @param outputPath
	 * 			path to output partial search to
	 * @throws IOException
	 */
	@Override
	public synchronized void writeAll(Path outputPath) throws IOException {

		minions.finish();

		try (BufferedWriter bw = Files.newBufferedWriter(outputPath,
				Charset.forName("UTF-8"))) {

			bw.write("{");

			if (!resultMap.isEmpty()) {
				Iterator<String> i = resultMap.keySet().iterator();
				String first = i.next();
				JSONWriter.writeLine(first, resultMap.get(first), bw);

				while (i.hasNext()) {
					String word = i.next();
					bw.write(",");
					JSONWriter.writeLine(word, resultMap.get(word), bw);
				}
			}
			bw.newLine();
			bw.write("}");

		} catch (IOException e) {
			System.err.println("Problem outputting partial search to " + outputPath);
		}
	}

	public Map<String, List<SearchResult>> getResult() {
		return Collections.unmodifiableMap(resultMap);
	}

}
