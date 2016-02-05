import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class PartialSearchAbstract {

	public void queryParser(Path path) throws IOException {

		try (BufferedReader bw = Files.newBufferedReader(path,
				Charset.forName("UTF-8"))) {

			String line;
			while ((line = bw.readLine()) != null) {
				parseLine(line);
			}

		}
	}

	public abstract void parseLine(String line);

	public abstract void writeAll(Path outputPath) throws IOException;

}
