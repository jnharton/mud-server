package mud.interfaces;

import mud.net.Client;

/**
 * Defines an interface for wieldable objects. (i.e. swords, shields, wands, etc)
 * 
 * @author Jeremy
 * 
 * @param <T> Some object type that will implement wieldable
 */
public interface Wieldable<T> {
	public void wield(String arg, Client client);
}