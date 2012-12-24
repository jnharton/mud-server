package mud.interfaces;

/**
 * Defines an interface to be used with any ridable object/creature, such as
 * an animal with is large enough, docile enough, and fast enough
 * to be used as an alternate form of humanoid transportation. Examples
 * might includes horses, ponies, large dogs, large cats, dragons (rarely),
 * and some exotic species.
 * 
 * @author Jeremy
 *
 */
public interface Ridable {
	
	/**
	 * mount/get in this ridable object
	 */
	public void mount();
	
	/**
	 * unmount/get out of this ridable object
	 */
	public void unmount();
	
	/**
	 * Is this ridable object big enough
	 * to fit you?
	 * 
	 * @return
	 */
	public boolean isLargeEnough();
}