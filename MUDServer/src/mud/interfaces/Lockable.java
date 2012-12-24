package mud.interfaces;

/**
 * Defines an interface for lockable objects.
 * 
 * @author Jeremy
 *
 * @param <L> Some object type that will implement Lockable
 */
public interface Lockable<L> {
	public void lock();
	public void unlock();
	
	public boolean isLocked();
}