package mud.interfaces;

public interface Closeable {
	public void open();
	public void close();
	public boolean isOpen();
}