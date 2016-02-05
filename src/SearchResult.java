/**
 * SearchResult class containing attributes, compareTo, and update method
 *
 * @author raywang
 *
 */
public class SearchResult implements Comparable<SearchResult> {

	/** Number of times any query word appeared at this path. */
	private int frequency;

	/** Earliest position any query word appeared at this path. */
	private int position;

	/** Path where one or more query words were found. */
	private final String path;

	/**
	 * Initializes a search result.
	 *
	 * @param frequency
	 * 			frequency of words
	 * @param position
	 * 			where the word appears in the file
	 * @param path
	 * 			path to file containing word
	 */
	public SearchResult(String path, int frequency, int position) {
		this.path = path;
		this.frequency = frequency;
		this.position = position;
	}

	/**
	 * Returns the number of times a query word was found.
	 *
	 * @return the number of times a query word was found.
	 */
	public int getFrequency() {
		return frequency;
	}

	/**
	 * Combines the frequency of multiple query words and finds the first
	 * position at which one of the query words appeared.
	 *
	 * @param frequency
	 * 			number of times the query words appeared
	 * @param position
	 * 			first position at which one of the query words appeared
	 */
	public void update(int frequency, int position) {
		this.frequency += frequency;

		if (this.position > position) {
			this.position = position;
		}
	}

	/**
	 * Returns the position of the query word that was found.
	 *
	 * @return the position of the query word that was found.
	 */
	public int getPosition() {
		return position;
	}

	/**
	 * Returns the path of the query word that was found.
	 *
	 * @return the path of the query word that was found.
	 */
	public String getPath() {
		return path;
	}

	/**
	 * overrides the compareTo
	 */
	@Override
	public int compareTo(SearchResult result) {

		if (this.frequency != result.frequency) {
			return Integer.compare(result.frequency, this.frequency);
		} else {
			if (this.position != result.position) {
				return Integer.compare(this.position, result.position);
			} else {
				return this.path.compareToIgnoreCase(result.path);
			}
		}

	}

	@Override
	public String toString() {
		return "Path: " + path + ",\n Position: " + position + ",\n Frequency: " + frequency;
	}

}
