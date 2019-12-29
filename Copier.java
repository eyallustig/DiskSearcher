import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 
 * A copier thread. Reads files to copy from a queue and copies them to the given destination.
 *
 */
public class Copier implements java.lang.Runnable {

	public static final int COPY_BUFFER_SIZE = 4096;
	private File destination;
	private SynchronizedQueue<File> resultsQueue;

	/**
	 * Constructor. Initializes the worker with a destination directory and a queue of files to copy.
	 * @param destination - Destination directory
	 * @param resultsQueue- Queue of files found, to be copied
	 */
	public Copier(java.io.File destination, SynchronizedQueue<java.io.File> resultsQueue) {
		this.destination = destination;
		this.resultsQueue = resultsQueue;
	}

	/**
	 * Runs the copier thread. 
	 * Thread will fetch files from queue and copy them, one after each other, to the destination directory. 
	 * When the queue has no more files, the thread finishes.
	 */
	public void run() {
		File source = resultsQueue.dequeue();
		while (source != null) {
			try {
				File dest = new File(destination.getAbsolutePath()+"\\"+source.getName());
				copyFileUsingStream(source, dest);
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Copier copied "+source.getName()+" successfully");
			source = resultsQueue.dequeue();
		}
	}

	private static void copyFileUsingStream(File source, File dest) throws IOException {
		InputStream is = null;
		OutputStream os = null;
		try {
			is = new FileInputStream(source);
			os = new FileOutputStream(dest);
			byte[] buffer = new byte[COPY_BUFFER_SIZE];
			int length;
			while ((length = is.read(buffer)) > 0) {
				os.write(buffer, 0, length);
			}
		} finally {
			is.close();
			os.close();
		}
	}
}