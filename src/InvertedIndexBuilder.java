import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Contains methods needed to build inverted index.
 *
 * @author raywang
 *
 */
public class InvertedIndexBuilder {

	/**
	 * Outputs the name of the file or subdirectory, with proper indentation
	 * to help indicate the hierarchy. If a subdirectory is encountered, will
	 * recursively list all the files in that subdirectory. If file is found
	 * ending in ".txt", we call addWords function.
	 *
	 * @param path to retrieve the listing, assumes a directory and not a file is passed
	 * @throws IOException
	 */
	public static void traverse(Path path, InvertedIndex index) throws IOException {
		try (DirectoryStream<Path> listing = Files.newDirectoryStream(path)) {
			for (Path file : listing) {
				if (Files.isDirectory(file)) {
					traverse(file, index);
				}
				else {
					if(file.toString().toLowerCase().endsWith(".txt")) {
						addWords(file, index);
					}
				}
			}
		}
	}

	/**
	 * Iterates through each file and adds word, path, and position to Map
	 *
	 * @param file
	 * @throws IOException
	 */
	public static void addWords(Path path, InvertedIndex index) throws IOException {

		int position = 0;

		try (BufferedReader read = Files.newBufferedReader(path)) {
			String line;
			while((line = read.readLine()) != null) {
				String[] words = split(line);
				for(String word : words) {
					if(!word.isEmpty()) {
						position++;
						String pathname = path.toString();
						index.add(word, pathname, position);
					}
				}
			}
		}

	}


	/**
	 * Removes whitespace, sets all to lower case, and removes special chars
	 *
	 * @param word
	 * @return word
	 */
	public static String clean(String word) {
		word = word.toLowerCase().trim();
		word = word.replaceAll(CLEAN_REGEX, "");
		return word;
	}

	/**
	 * Splits a string of words using SPLIT_REGEX
	 * @param text
	 * @return
	 */
	public static String[] split(String text) {
		text = clean(text);
		String [] words;
		if(!(text.length() == 0)) {
			words = text.split(SPLIT_REGEX);
		}
		else {
			words = new String [] {};
		}
		return words;
	}

	/* Regular expression for splitting text into words by whitespace. */
	public static final String SPLIT_REGEX = "(?U)\\p{Space}+";

	/* Regular expression for removing special characters. */
	public static final String CLEAN_REGEX = "(?U)[^\\p{Alnum}\\p{Space}]+";

}
