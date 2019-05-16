import java.util.NoSuchElementException;

/**
 * This class is provide a immutable Queue functions.
 * 
 * Author: Vaithiynadhan 
 * Version: 1.0
 */
public final class ImmutableQueue<T> implements Queue<T> {

	private final Object[] originalObject;

	static Object[] copyObject = new Object[16];
	static int h;
	static int t;

	/**
	 * Constructor used to add element the immutable Object
	 */
	public ImmutableQueue() {
		originalObject = copyObject;
	}

	/**
	 * Constructor used to removeElement the immutable Object
	 */
	public ImmutableQueue(Object[] copyObject) {
		originalObject = copyObject;
		for (int i = 1; i < copyObject.length; i++) {
			originalObject[i - 1] = copyObject[i];
		}
	}

	/**
	 * Adding element to the Queue at first position.
	 */
	public ImmutableQueue<T> enQueue(T e) {
		if (e == null)
			throw new NullPointerException();
		copyObject[t] = e;
		if ((t = (t + 1) & (copyObject.length - 1)) == h)
			increaseSizeOfArray();

		return new ImmutableQueue<T>();
	}

	/**
	 * removing first position element in the Queue.
	 */
	public ImmutableQueue<T> deQueue() {
		@SuppressWarnings("unchecked")
		T result = (T) copyObject[h];
		if (result == null)
			return null;
		h = (h + 1) & (copyObject.length - 1);
		return new ImmutableQueue<T>(copyObject);
	}

	/**
	 * return the head value of a queue.
	 */
	public T head() {
		int h = 0;
		@SuppressWarnings("unchecked")
		T result = (T) originalObject[h];
		if (result == null)
			return null;
		return result;
	}

	/**
	 * Check whether the Queue is empty or not.
	 */
	public boolean isEmpty() {
		@SuppressWarnings("unchecked")
		T result = (T) originalObject[h];
		if (result == null)
			return true;
		return false;
	}

	public T get(int i) {
		@SuppressWarnings("unchecked")
		T result = (T) originalObject[i];
		if (result == null)
			throw new NoSuchElementException();
		return result;
	}

	/**
	 * return the size of the Queue.
	 */
	public int size() {
		return (t - h) & (originalObject.length - 1);
	}

	/**
	 * Increase size twice to make the Queue dynamically increase.
	 */
	private void increaseSizeOfArray() {
		assert h == t;
		int headValue = h;
		int lengthOfCopyObject = copyObject.length;
		int remainLength = lengthOfCopyObject - headValue;
		int arrayLength = lengthOfCopyObject << 1;
		Object[] tempObject = new Object[arrayLength];
		System.arraycopy(copyObject, headValue, tempObject, 0, remainLength);
		System.arraycopy(copyObject, 0, tempObject, remainLength, headValue);
		copyObject = tempObject;
		h = 0;
		t = lengthOfCopyObject;
	}
}
