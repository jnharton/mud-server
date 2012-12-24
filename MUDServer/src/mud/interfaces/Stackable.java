package mud.interfaces;

/**
 * Defines an interface for "stackable" objects. (money?)
 * 
 * @author Jeremy
 *
 * @param <E> Some object type that will implement stackable.
 */
public interface Stackable<E> {
	public int maxDepth = 10;
	
	/**
	 * Takes a stackable object e and returns a boolean
	 * indicating whether it could be stacked, in theory I could
	 * try to stack potions and arrows, but that would be invalid.
	 * So stacking identical arrow types with each other would work.
	 * 
	 * i.e.
	 * Flaming Arrow (1) stacks with Flaming Arrow (1) to give Flaming Arrow(2)
	 * 
	 * @param object
	 * @return
	 */
	boolean stack(E object);
	
	/**
	 * Split a stack, the number supplied is how many you'd like to split
	 * from the given stack.
	 * 
	 * @param number
	 * @return
	 */
	E split(int number);
	
	/**
	 * Calculate and return the size of the stack, use of recursion
	 * is recommended in implementation.
	 * 
	 * @return
	 */
	int stackSize();
}