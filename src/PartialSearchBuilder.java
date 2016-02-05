import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores PartialSearch data structure with necessary methods
 *
 * @author raywang
 *
 */

public class PartialSearchBuilder extends PartialSearchAbstract {

	/* Stores query line and list of Search Results */
	private final Map<String, List<SearchResult>> resultMap;

	/* Stores word and relative path and position */
	private InvertedIndex index;

	/* Initializes LinkedHashMap */
	public PartialSearchBuilder(InvertedIndex index) {
		resultMap = new LinkedHashMap<>();
		this.index = index;
	}

	/**
	 * parses each query line into an array, calls partial search method
	 * and adds path and List of Search Results to map
	 * @param line
	 * @param index
	 */
	@Override
	public void parseLine(String line) {
		String [] queries = InvertedIndexBuilder.split(line);
		List<SearchResult> resultsList = index.partialSearch(queries);

		resultMap.put(line, resultsList);
	}

	/**
	 * writes entire partial search
	 * @param outputPath
	 * 			path to output partial search to
	 * @throws IOException
	 */
	@Override
	public void writeAll(Path outputPath) throws IOException {

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
}
