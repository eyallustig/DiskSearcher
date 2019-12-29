import java.io.File;

/**
 * 
 * Main application class. 
 * This application search for all files under some given path that contain a given textual pattern. 
 * All files found are copied to some specific directory.
 *
 */
public class DiskSearcher {

	public static final int DIRECTORY_QUEUE_CAPACIT = 50;
	public static final int RESULTS_QUEUE_CAPACITY = 50;

	/**
	 * Main method. Reads arguments from command line and starts the search.
	 * @param args - Command line arguments
	 */
	public static void main(java.lang.String[] args) {
		String pattern = args[0];
		File rootDirectory = new File(args[1]);
		File destinationDirectory = new File(args[2]);
		int numberOfSearchers = Integer.parseInt(args[3]);;
		int numberOfCopiers = Integer.parseInt(args[4]);

		// If destination path doesn't exist - create it
		if (!destinationDirectory.exists()) {
			destinationDirectory.mkdirs();
			System.out.println("Created destination directory");
		}

		// In case the root doesn't exist - print a usage message to screen
		if (!rootDirectory.exists()) {
			System.out.println("Root directory doesn't exist");
			return;
		}

		// Number of searchers and number of copiers should not be negative ( if it is, print a message)
		if (numberOfSearchers < 1) {
			System.out.println("Number of searchers is negative, please enter a positive number of searchers");
			return;
		}
		if (numberOfCopiers < 1) {
			System.out.println("Number of copiers is negative, please enter a positive number of copiers");
			return;
		}

		SynchronizedQueue<java.io.File> directoryQueue = new SynchronizedQueue<>(DIRECTORY_QUEUE_CAPACIT);
		SynchronizedQueue<java.io.File> resultsQueue = new SynchronizedQueue<>(RESULTS_QUEUE_CAPACITY);

		// Start a single scouter thread
		Thread scouter = new Thread(new Scouter(directoryQueue, rootDirectory));
		scouter.start();

		// Start a group of searcher threads (number of searchers as specified in arguments)
		Thread[] searchers = new Thread[numberOfSearchers];
		for (int i = 0 ; i < numberOfSearchers ; i++) {
			searchers[i] = new Thread(new Searcher(pattern, directoryQueue, resultsQueue));
			searchers[i].start();
		}

		// Start a group of copier threads (number of copiers as specified in arguments)
		Thread[] copiers = new Thread[numberOfCopiers];
		for (int i = 0 ; i < numberOfCopiers ; i++) {
			copiers[i] = new Thread(new Copier(destinationDirectory, resultsQueue));
			copiers[i].start();
		}

		// Wait for scouter to finish
		try {
			scouter.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Wait for searcher and copier threads to finish
		for (int i = 0 ; i < numberOfSearchers ; i++) {
			try {
				searchers[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		for (int i = 0 ; i < numberOfCopiers ; i++) {
			try {
				copiers[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
