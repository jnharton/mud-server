package mud.interfaces;

import mud.net.Client;

/**
 * Defines a interface for npcs, etc that can be interacted with
 * 
 * @author Jeremy
 *
 */
public interface Interactive {
	public abstract void interact(Client client);
	//public abstract void say(String message, String playerName);
}