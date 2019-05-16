import java.io.Serializable;
import java.util.NoSuchElementException;

public final class QueueTest<T> implements ValueAdd<T>, Cloneable, Serializable {

	      private final Object[] queObj;
	      private final int h;
	      private final int t;

		  static Object[] elements = new Object[16];
		  static int head;
		  static int tail;

	    /**
	     * Doubles the capacity of this deque.  Call only when full, i.e.,
	     * when head and tail have wrapped around to become equal.
	     */
	    private void doubleCapacity() {
	        assert head == tail;
	        int p = head;
	        int n = elements.length;
	        int r = n - p; // number of elements to the right of p
	        int newCapacity = n << 1;
	        if (newCapacity < 0)
	            throw new IllegalStateException("Sorry, deque too big");
	        Object[] a = new Object[newCapacity];
	        System.arraycopy(elements, p, a, 0, r);
	        System.arraycopy(elements, 0, a, r, p);
	        elements = a;
	        head = 0;
	        tail = n;
	    }

	    public int size() {
	        return (t - h) & (queObj.length - 1);
	    }


	    public QueueTest() {
	        queObj = elements;
	        h = head;
	        t = tail;
	    }


		public QueueTest add(T e) {
	        if (e == null)
	            throw new NullPointerException();
	        elements[tail] = e;
	        if ( (tail = (tail + 1) & (elements.length - 1)) == head)
	            doubleCapacity();
	        return new QueueTest();

	    }

	    /**
	     * @throws NoSuchElementException {@inheritDoc}
	     */
	    public T get(int i) {
	        @SuppressWarnings("unchecked")
	        T result = (T) queObj[i];
	        if (result == null)
	            throw new NoSuchElementException();
	        return result;
	    }

}