package mud.interfaces;

public interface Storage<T> {
	public T retrieve(int index);
	public T retrieve(String tName);
	public void insert(T i);
	public boolean isFull();
}