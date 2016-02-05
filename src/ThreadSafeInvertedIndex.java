import java.nio.file.Path;
import java.util.List;

/**
 * Stores the inverted index with thread safe methods.
 *
 * @author raywang
 *
 */
public class ThreadSafeInvertedIndex extends InvertedIndex {

	private ReadWriteLock lock;

	public ThreadSafeInvertedIndex() {
		super();
		lock = new ReadWriteLock();
	}

	@Override
	public void add(String word, String path, int position) {
		lock.lockReadWrite();
		try {
			super.add(word, path, position);
		} finally {
			lock.unlockReadWrite();
		}
	}

	@Override
	public void writeAll(Path output) {
		lock.lockReadOnly();
		try {
			super.writeAll(output);
		} finally {
			lock.unlockReadOnly();
		}
	}

	@Override
	public List<SearchResult> partialSearch(String [] queries) {
		lock.lockReadOnly();
		try {
			return super.partialSearch(queries);
		} finally {
			lock.unlockReadOnly();
		}
	}


	@Override
	public void addAll(InvertedIndex other) {
		lock.lockReadWrite();
		try {
			super.addAll(other);
		} finally {
			lock.unlockReadWrite();
		}
	}
}
