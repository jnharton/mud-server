package mud.interfaces;

/**
 * Defines an interface for "editable" objects. (book, scroll, letter, board)
 * @author Jeremy
 *
 */
public interface Editable {
	public String read();
	public void write(String s);
}