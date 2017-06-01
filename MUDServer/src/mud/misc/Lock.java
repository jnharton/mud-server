package mud.misc;

public interface Lock {
	public void lock();
	public void unlock();
	public boolean isLocked();
}