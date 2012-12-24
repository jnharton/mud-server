package mud.interfaces;

import mud.net.Client;

/**
 * Defines an interface for wearable objects, such as clothes. (i.e. armor, clothing, cloak, boots, etc)
 * 
 * @author Jeremy
 * 
 * @param <T> Some object type that will implement wearable.
 */
public interface Wearable<T> {
	public void wear(String arg, Client client);
}