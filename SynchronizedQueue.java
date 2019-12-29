/**
 * A synchronized bounded-size queue for multithreaded producer-consumer applications.
 * 
 * @param <T> Type of data items
 */
public class SynchronizedQueue<T> {

	private T[] buffer;
	private int producers;
	private final Object lock = new Object();
	private int capacity;
	private int size;
	private int first;
	private int last;

	/**
	 * Constructor. Allocates a buffer (an array) with the given capacity and
	 * resets pointers and counters.
	 * @param capacity Buffer capacity
	 */
	@SuppressWarnings("unchecked")
	public SynchronizedQueue(int capacity) {
		this.buffer = (T[])(new Object[capacity]);
		this.producers = 0;
		this.capacity = capacity;
		this.size = 0;
		this.first = 0;
		this.last = 0;
	}

	/**
	 * Dequeues the first item from the queue and returns it.
	 * If the queue is empty but producers are still registered to this queue, 
	 * this method blocks until some item is available.
	 * If the queue is empty and no more items are planned to be added to this 
	 * queue (because no producers are registered), this method returns null.
	 * 
	 * @return The first item, or null if there are no more items
	 * @see #registerProducer()
	 * @see #unregisterProducer()
	 */
	public T dequeue() {
		synchronized (lock) {
			while (size == 0 && producers > 0) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (size == 0 && producers == 0) {
				return null;
			}
			T firstItem = buffer[first % capacity];
			first++;
			size--;
			// notify waiting threads that some space becomes available
			lock.notifyAll();
			return firstItem;
		}
	}

	/**
	 * Enqueues an item to the end of this queue. If the queue is full, this 
	 * method blocks until some space becomes available.
	 * 
	 * @param item Item to enqueue
	 */
	public void enqueue(T item) {
		synchronized (lock) {
			while (size == capacity) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			buffer[last % capacity] = item;
			size++;
			last++;
			// notify waiting threads some item is available
			lock.notifyAll();
		}
	}

	/**
	 * Returns the capacity of this queue
	 * @return queue capacity
	 */
	public int getCapacity() {
		return this.capacity;
	}

	/**
	 * Returns the current size of the queue (number of elements in it)
	 * @return queue size
	 */
	public int getSize() {
		synchronized (lock) {
			return this.size;						
		}
	}

	/**
	 * Registers a producer to this queue. This method actually increases the
	 * internal producers counter of this queue by 1. This counter is used to
	 * determine whether the queue is still active and to avoid blocking of
	 * consumer threads that try to dequeue elements from an empty queue, when
	 * no producer is expected to add any more items.
	 * Every producer of this queue must call this method before starting to 
	 * enqueue items, and must also call <see>{@link #unregisterProducer()}</see> when
	 * finishes to enqueue all items.
	 * 
	 * @see #dequeue()
	 * @see #unregisterProducer()
	 */
	public void registerProducer() {
		// This should be in a critical section
		synchronized (this.lock) {
			this.producers++;			
		}
	}

	/**
	 * Unregisters a producer from this queue. See <see>{@link #registerProducer()}</see>.
	 * 
	 * @see #dequeue()
	 * @see #registerProducer()
	 */
	public void unregisterProducer() {
		// This should be in a critical section
		synchronized (this.lock) {
			this.producers--;
			lock.notifyAll();
		}
	}
}
