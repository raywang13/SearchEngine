import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Contains methods needed to build inverted index using multithreading.
 *
 * @author raywang
 *
 */
public class MultithreadedInvertedIndexBuilder {

	/* Logger to help debug */
	private static final Logger logger = LogManager.getLogger();

	/* Work Queue for threads */
	private final WorkQueue minions;

	/* Initializes WorkQueue and sets pending to 0 */
	public MultithreadedInvertedIndexBuilder(int threadCount) {
		minions = new WorkQueue(threadCount);
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

		private Path path;
		private ThreadSafeInvertedIndex index;

		public Minion(Path path, ThreadSafeInvertedIndex index) {
			this.path = path;
			this.index = index;
			logger.debug("New minion created.");
		}

		@Override
		public void run() {
			try {
				InvertedIndex local = new InvertedIndex();
				InvertedIndexBuilder.addWords(path, local);
				logger.debug("Adding words {}", path);
				index.addAll(local);
			} catch (IOException e) {
				logger.debug("Unable to parse {}", path);
			} finally {
				logger.debug("Finished {}", path);
			}
		}
	}

	/**
	 * Outputs the name of the file or subdirectory, with proper indentation
	 * to help indicate the hierarchy. If a subdirectory is encountered, will
	 * recursively list all the files in that subdirectory. If a file is found
	 * ending in ".txt", we call minions.execute().
	 *
	 * @param path to retrieve the listing, assumes a directory and not a file is passed
	 * @throws IOException
	 */
	public synchronized void traverse(Path path, ThreadSafeInvertedIndex index)
			throws IOException {
		try (DirectoryStream<Path> listing = Files.newDirectoryStream(path)) {
			for (Path file : listing) {
				if (Files.isDirectory(file)) {
					traverse(file, index);
				}
				else {
					if(file.toString().toLowerCase().endsWith(".txt")) {
						minions.execute(new Minion(file, index));
					}
				}
			}
		}
	}
}
