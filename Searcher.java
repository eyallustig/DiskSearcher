import java.io.File;
import java.io.FilenameFilter;

/**
 * 
 * A searcher thread. Searches for files with a given pattern in all directories listed in a directory queue.
 *
 */
public class Searcher implements java.lang.Runnable {

	private String pattern;
	private SynchronizedQueue<File> directoryQueue;
	private SynchronizedQueue<File> resultsQueue;

	/**
	 * Constructor. Initializes the searcher thread.
	 * @param pattern - Pattern to look for
	 * @param directoryQueue - A queue with directories to search in (as listed by the scouter)
	 * @param resultsQueue - A queue for files found (to be copied by a copier)
	 */
	public Searcher(java.lang.String pattern, SynchronizedQueue<java.io.File> directoryQueue, SynchronizedQueue<java.io.File> resultsQueue) {
		this.pattern = pattern;
		this.directoryQueue = directoryQueue;
		this.resultsQueue = resultsQueue;
	}

	/**
	 * Runs the searcher thread. 
	 * Thread will fetch a directory to search in from the directory queue, then search all files inside it (but will not recursively search subdirectories!). 
	 * Files that are found to have the given pattern are enqueued to the results queue. 
	 * This method begins by registering to the results queue as a producer and when finishes, it unregisters from it.
	 */
	public void run() {
		resultsQueue.registerProducer();
		File directory = directoryQueue.dequeue();
		while (directory != null) {
			File[] filesToEnqueue = directory.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.contains(pattern);
				}
			});
			for (File file : filesToEnqueue) {
				System.out.println("Searcher added "+file.getName()+" to resultQueue");
				resultsQueue.enqueue(file);
			}	
			directory = directoryQueue.dequeue();
		}
		resultsQueue.unregisterProducer();
	}
}