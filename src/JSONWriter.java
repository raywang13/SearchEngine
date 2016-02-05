import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;
/**
 * Contains all methods necessary to produce JSON output.
 *
 * @author raywang
 *
 */

public class JSONWriter {

	/**
	 * Writes a String and the Map value for that String.
	 * @param elements
	 * 			TreeMap words -> TreeMap path -> TreeSet positions
	 * @param bw
	 * 			writer to use
	 * @throws IOException
	 */
	public static void writeWord(Entry<String,
			TreeMap<String, TreeSet<Integer>>> element, BufferedWriter bw) throws IOException {

		TreeMap<String, TreeSet<Integer>> map = element.getValue();

		bw.newLine();
		bw.write(indent(1) + quote(element.getKey()) + ": {");
		if (!map.isEmpty()) {
			Entry<String, TreeSet<Integer>> first = map.firstEntry();
			writePath(first, bw, 1);

			for (Entry<String, TreeSet<Integer>> entry :
				map.tailMap(first.getKey(), false).entrySet()) {
				bw.write(",");
				writePath(entry, bw, 1);
			}
		}

		bw.newLine();
		bw.write(indent(1) + "}");

	}

	/**
	 * Writes out a String and the Set value for that String
	 * @param elements
	 * 			TreeMap path -> TreeSet of positions
	 * @param bw
	 * 			writer to use
	 * @param level
	 * 			indent level
	 * @throws IOException
	 */
	public static void writePath(Entry<String,
			TreeSet<Integer>> elements, BufferedWriter bw, int level) throws IOException {

		if (!elements.getValue().isEmpty()) {
			TreeSet<Integer> set = elements.getValue();
			bw.newLine();
			bw.write(indent(level + 1) + quote(elements.getKey()) + ": [");
			writePosition(set, bw, 1);
		}

		bw.newLine();
		bw.write(indent(level + 1) + "]");

	}


	/**
	 * Writes a set of integers in JSON format.
	 * @param elements
	 * 			elements to write to file
	 * @param bw
	 * 			writer to use
	 * @param level
	 * 			level of indent
	 * @throws IOException
	 */
	public static void writePosition(TreeSet<Integer> elements,
			BufferedWriter bw, int level) throws IOException {

		if (!elements.isEmpty()) {
			Integer first = elements.first();
			bw.newLine();
			bw.write(indent(level + 2) + first);

			for (Integer entry : elements.tailSet(first, false)) {
				bw.write(",");
				bw.newLine();
				bw.write(indent(level + 2) + entry);
			}
		}

	}

	/**
	 * Writes a key from a map
	 * @param word
	 * 			word to be written
	 * @param resultsList
	 * 			List of SearchResult
	 * @param bw
	 * 			Buffered Writer
	 * @throws IOException
	 */
	public static void writeLine(String word, List<SearchResult> resultsList,
			BufferedWriter bw) throws IOException {

		bw.newLine();
		bw.write(indent(1) + quote(word));
		bw.write(": [");
		if (!resultsList.isEmpty()) {
			writeResults(resultsList, bw);
		}
		bw.newLine();
		bw.write(indent(1) + "]");

	}

	/**
	 * Writes a list of SearchResult
	 * @param searchResults
	 * 			List of SearchResults to be written
	 * @param bw
	 * 			BufferedWriter
	 * @throws IOException
	 */
	public static void writeResults(List<SearchResult> searchResults,
			BufferedWriter bw) throws IOException {

		SearchResult first = searchResults.get(0);
		writeResult(first, bw);

		for (int i = 1; i < searchResults.size(); i++) {
			bw.write(",");
			writeResult(searchResults.get(i), bw);
		}
	}

	/**
	 * Writes one SearchResult in JSON format
	 * @param searchResult - SearchResult to be written
	 * @param bw - BufferedWriter
	 * @throws IOException
	 */
	public static void writeResult(SearchResult searchResult,
			BufferedWriter bw) throws IOException {

		bw.newLine();
		bw.write(indent(2) + "{");
		bw.newLine();

		bw.write(indent(3) + quote("where") + ": " + quote(searchResult.getPath()) + ",");
		bw.newLine();

		bw.write(indent(3) + quote("count") + ": " +
				searchResult.getFrequency() + ",");
		bw.newLine();

		bw.write(indent(3) + quote("index") + ": " + searchResult.getPosition());
		bw.newLine();

		bw.write(indent(2) + "}");
	}

	/**
	 * Helper method to indent several times by 2 spaces each time. For example,
	 * indent(0) will return an empty string, indent(1) will return 2 spaces,
	 * and indent(2) will return 4 spaces.
	 *
	 * <p>
	 * <em>Using this method is optional!</em>
	 * </p>
	 *
	 * @param times
	 * @return
	 * @throws IOException
	 */
	public static String indent(int times) throws IOException {
		return times > 0 ? String.format("%" + times * 2 + "s", " ") : "";
	}

	/**
	 * Helper method to quote text for output. This requires escaping the
	 * quotation mark " as \" for use in Strings. For example:
	 *
	 * <pre>
	 * String text = "hello world";
	 * System.out.println(text); // output: hello world
	 * System.out.println(quote(text)); // output: "hello world"
	 * </pre>
	 *
	 * @param text
	 *            input to surround with quotation marks
	 * @return quoted text
	 */
	public static String quote(String text) {
		return "\"" + text + "\"";
	}


}
