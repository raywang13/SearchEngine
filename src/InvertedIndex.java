import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Stores the inverted index with necessary methods
 *
 * @author raywang
 *
 */
public class InvertedIndex {

	/* Stores word and relative path and position */
	private final TreeMap<String, TreeMap<String, TreeSet<Integer>>> index;

	/* Initializes TreeMap */
	public InvertedIndex() {
		index = new TreeMap<>();
	}

	/**
	 * Properly adds a word and position to the index. Must initialize inner
	 * data structure if necessary. Make sure you consider how to handle
	 * duplicates (duplicate words, and words with duplicate positions), and how
	 * to handle words with mixed case and extra spaces.
	 *
	 * @param word
	 *            word to add to index
	 * @param position
	 *            position word was found
	 * @return true if this was a unique entry, false if no changes were made to
	 *         the index
	 */
	public void add(String word, String path, int position) {

		String pathString = path.toString();

		if (!index.containsKey(word)) {
			index.put(word, new TreeMap<String, TreeSet<Integer>>());
		}

		if (!index.get(word).containsKey(pathString)) {
			index.get(word).put(pathString, new TreeSet<Integer>());
		}

		index.get(word).get(pathString).add(position);
	}

	/**
	 * Writes the entire nested data structure.
	 * @param output
	 * 			output path
	 */
	public void writeAll(Path output) {

		try (BufferedWriter bw = Files.newBufferedWriter(output, Charset.forName("UTF8"))) {

			bw.write("{");
			if (!index.isEmpty()) {
				Entry<String, TreeMap<String, TreeSet<Integer>>> first
				= index.firstEntry();

				JSONWriter.writeWord(first, bw);

				for(Entry<String, TreeMap<String, TreeSet<Integer>>> entry :
					index.tailMap(first.getKey(), false).entrySet()) {
					bw.write(",");

					JSONWriter.writeWord(entry, bw);
				}

				bw.newLine();
				bw.write("}");
				bw.newLine();
			}
		} catch (IOException e) {
			System.err.println("Problem outputting index to " + output + ".");
		}
	}

	/**
	 * builds sorted List of SearchResults
	 * @param queries
	 * 			array query words
	 * @return
	 * 			sorted List of SearchResults
	 */
	public List<SearchResult> partialSearch(String [] queries) {

		int frequency = 0;
		int position;

		/* temporary data structure to store results and maintain frequency,
		 * path, and position
		 */
		Map<String, SearchResult> searchResults = new HashMap<>();

		/* List of search results */
		List<SearchResult> resultsList = new ArrayList<SearchResult>();

		/* iterates through array of queries */
		for (String query : queries) {

			/* iterates through inverted index */
			for (String word : index.tailMap(query).keySet()) {

				if (word.startsWith(query)) {
					/* iterates through paths of each word in inverted index */
					for (String path : index.get(word).keySet()) {

						frequency = index.get(word).get(path).size();
						position = index.get(word).get(path).first();

						/* if path already exists in the Map, combine frequencies */
						if (searchResults.containsKey(path)) {
							searchResults.get(path).update(frequency, position);
						} else {
							SearchResult searchResult = new SearchResult(path, frequency, position);
							searchResults.put(path, searchResult);
							resultsList.add(searchResult);
						}
					}
				} else {
					break;
				}
			}
		}

		Collections.sort(resultsList);

		return resultsList;
	}

	/**
	 * Adds each local inverted index into the global inverted index
	 *
	 * @param other
	 */
	public void addAll(InvertedIndex other) {
		for (String word : other.index.keySet()) {
			if (!this.index.containsKey(word)) {
				this.index.put(word, other.index.get(word));
			} else {
				for (String path: other.index.get(word).keySet()) {
					if (!index.get(word).containsKey(path)) {
						this.index.get(word).put(path, other.index.get(word).get(path));
					} else {
						this.index.get(word).put(path, other.index.get(word).get(path));
					}
				}
			}
		}
	}

}
