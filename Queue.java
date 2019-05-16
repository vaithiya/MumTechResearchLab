
/**
 * This Queue interface is define the methods of Immutable Queue.
 * 
 * Author: Vaithiynadhan
 * Version: 1.0
 */
public interface Queue<T> {
	public Queue<T> enQueue(T t);
    public Queue<T> deQueue();
    public T head();
    public boolean isEmpty();
}
