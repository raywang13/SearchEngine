import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;


/* ssh ywang103@stargate.cs.usfca.edu -L 3307:sql.cs.usfca.edu:3306 -N */

/**
 * This software driver class provides a consistent entry point for the search
 * engine. Based on the arguments provided to {@link #main(String[])}, it
 * creates the necessary objects and calls the necessary methods to build an
 * inverted index, process search queries, configure multithreading, and launch
 * a web server (if appropriate).
 */
public class Driver {

	/**
	 * Flag used to indicate the following value is an input directory of text
	 * files to use when building the inverted index.
	 *
	 * @see "Projects 1 to 5"
	 */
	public static final String INPUT_FLAG = "-input";

	/**
	 * Flag used to indicate the following value is the path to use when
	 * outputting the inverted index to a JSON file. If no value is provided,
	 * then {@link #INDEX_DEFAULT} should be used. If this flag is not provided,
	 * then the inverted index should not be output to a file.
	 *
	 * @see "Projects 1 to 5"
	 */
	public static final String INDEX_FLAG = "-index";

	/**
	 * Flag used to indicate the following value is a text file of search
	 * queries.
	 *
	 * @see "Projects 2 to 5"
	 */
	public static final String QUERIES_FLAG = "-query";

	/**
	 * Flag used to indicate the following value is the path to use when
	 * outputting the search results to a JSON file. If no value is provided,
	 * then {@link #RESULTS_DEFAULT} should be used. If this flag is not
	 * provided, then the search results should not be output to a file.
	 *
	 * @see "Projects 2 to 5"
	 */
	public static final String RESULTS_FLAG = "-results";

	/**
	 * Flag used to indicate the following value is the number of threads to use
	 * when configuring multithreading. If no value is provided, then
	 * {@link #THREAD_DEFAULT} should be used. If this flag is not provided,
	 * then multithreading should NOT be used.
	 *
	 * @see "Projects 3 to 5"
	 */
	public static final String THREAD_FLAG = "-threads";

	/**
	 * Flag used to indicate the following value is the seed URL to use when
	 * building the inverted index.
	 *
	 * @see "Projects 4 to 5"
	 */
	public static final String SEED_FLAG = "-seed";

	/**
	 * Flag used to indicate the following value is the port number to use when
	 * starting a web server. If no value is provided, then
	 * {@link #PORT_DEFAULT} should be used. If this flag is not provided, then
	 * a web server should not be started.
	 */
	public static final String PORT_FLAG = "-port";

	/**
	 * Default to use when the value for the {@link #INDEX_FLAG} is missing.
	 */
	public static final String INDEX_DEFAULT = "index.json";

	/**
	 * Default to use when the value for the {@link #RESULTS_FLAG} is missing.
	 */
	public static final String RESULTS_DEFAULT = "results.json";

	/**
	 * Default to use when the value for the {@link #THREAD_FLAG} is missing.
	 */
	public static final int THREAD_DEFAULT = 5;

	/**
	 * Default to use when the value for the {@link #PORT_FLAG} is missing.
	 */
	public static final int PORT_DEFAULT = 8080;

	/**
	 * Parses the provided arguments and, if appropriate, will build an inverted
	 * index from a directory or seed URL, process search queries, configure
	 * multithreading, and launch a web server.
	 *
	 * @param args
	 *            set of flag and value pairs
	 */
	public static void main(String[] args) {

		ArgumentParser parser = new ArgumentParser(args);
		Logger logger = LogManager.getLogger();
		InvertedIndex index = null;
		PartialSearchAbstract searcher = null;
		Server server = null;

		Path outputPath = null;
		Path inputPath = null;
		Path resultPath = null;

		if (parser.hasFlag(THREAD_FLAG) || parser.hasFlag(SEED_FLAG)) { /* Multithreaded */

			logger.debug("Thread flag found, executing multithreading.");

			int threadCount = THREAD_DEFAULT;

			try {
				threadCount = Integer.parseInt(parser.getValue(THREAD_FLAG));
				if (threadCount <= 0) {
					threadCount = THREAD_DEFAULT;
				}
			} catch (NumberFormatException e) {
				System.err.println("Invalid thread count input.");
			}
			logger.debug("thread count: " +threadCount);

			ThreadSafeInvertedIndex safe = new ThreadSafeInvertedIndex();
			index = safe;
			searcher =
					new MultithreadedPartialSearchBulider(threadCount, (ThreadSafeInvertedIndex) index);
			MultithreadedInvertedIndexBuilder invertedIndexBuilder =
					new MultithreadedInvertedIndexBuilder(threadCount);

			if (parser.hasFlag(INPUT_FLAG)) {
				if (parser.hasValue(INPUT_FLAG)) {
					inputPath = Paths.get(parser.getValue(INPUT_FLAG));
					try {
						invertedIndexBuilder.traverse(inputPath, (ThreadSafeInvertedIndex) index);
						invertedIndexBuilder.shutdown();
					} catch (IOException e) {
						System.err.println("File not found. File name: " + inputPath.toString());
					}
					logger.debug("End traverse directory.");
				}
			} else {
				System.err.println("No input directory.");
			}

			if (parser.hasFlag(SEED_FLAG)) {
				WebCrawler crawler = new WebCrawler(threadCount, (ThreadSafeInvertedIndex) index);
				try {
					crawler.traverse(parser.getValue(SEED_FLAG));
				} catch (IOException e) {
					System.err.println("Invalid link");
				}
			}
			invertedIndexBuilder.shutdown();
			logger.debug("Shut down SEED Index");

			/* Server */
			if (parser.hasFlag(PORT_FLAG)) {
				try {
					DatabaseConnector test =
							new DatabaseConnector("database.properties");
					System.out.println("Connecting: " +test.uri);

					if (test.testConnection()) {
						System.out.println("Connection established.");
					} else {
						System.err.println("Unable to connect.");
						return;
					}
				} catch (Exception e) {
					System.err.println("Unable to connect.");
					System.err.println(e.getMessage());
				}

				if (parser.hasValue(PORT_FLAG)) {
					server = new Server(Integer.parseInt(parser.getValue(PORT_FLAG)));
				} else {
					server = new Server(PORT_DEFAULT);
				}

				ServletContextHandler handler =
						new ServletContextHandler();

				handler.setContextPath("/");
				handler.addServlet(new ServletHolder(new CoreServlet(threadCount, index)), "/");
				handler.addServlet(LoginUserServlet.class, "/login");
				handler.addServlet(LoginRegisterServlet.class, "/register");

				server.setHandler(handler);
				try {
					server.start();
					server.join();
				} catch (Exception e) {
					System.err.println("Could not start server.");
				}
			}

			/* if searching */
			if (parser.hasFlag(QUERIES_FLAG)) {
				if (parser.hasValue(QUERIES_FLAG)) {
					try {
						searcher.queryParser(
								Paths.get(parser.getValue(QUERIES_FLAG)));
						((MultithreadedPartialSearchBulider) searcher).shutdown();
					} catch (Exception e) {
						System.err.println("Query file not found.");
					}
				}
			}
			logger.debug("End partial search.");

		} else { /*Single Threaded */

			logger.debug("No thread flag, executing single thread.");

			index = new InvertedIndex();
			searcher = new PartialSearchBuilder(index);

			if (parser.hasFlag(INPUT_FLAG)) {
				if (parser.hasValue(INPUT_FLAG)) {
					inputPath = Paths.get(parser.getValue(INPUT_FLAG));

					try {
						InvertedIndexBuilder.traverse(inputPath, index);
					} catch (IOException e) {
						System.err.println("File not found. File name: " + inputPath.toString());
					}

				}
			} else {
				System.err.println("No input directory.");
			}

			/* if searching */
			if (parser.hasFlag(QUERIES_FLAG)) {
				if (parser.hasValue(QUERIES_FLAG)) {
					try {
						searcher.queryParser(
								Paths.get(parser.getValue(QUERIES_FLAG)));
					} catch (Exception e) {
						System.err.println("Query file not found.");
					}
				}
			}

		}

		/* Writes index */
		if (parser.hasFlag(INDEX_FLAG)) {
			if (parser.hasValue(INDEX_FLAG) == false) {
				outputPath = Paths.get(INDEX_DEFAULT);
			} else {
				outputPath = Paths.get(parser.getValue(INDEX_FLAG));
			}
			logger.debug("Output path is: " +outputPath);
			index.writeAll(outputPath);
		} else {
			System.err.print("No output directory.");
		}

		/* Writes results */
		if (parser.hasFlag(RESULTS_FLAG)) {
			if (parser.hasValue(RESULTS_FLAG)) {
				resultPath = Paths.get(parser.getValue(RESULTS_FLAG));
			} else {
				resultPath = Paths.get(RESULTS_DEFAULT);
			}
			try {
				searcher.writeAll(resultPath);
			} catch (IOException e) {
				System.err.println("Could not output partial search to " + resultPath);
			}
		} else {
			System.err.println("No output directory for partial search.");
		}
	}
}
