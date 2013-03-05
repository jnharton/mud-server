package mud.commands;

import mud.MUDServer;
import mud.net.Client;
import mud.objects.Player;
import mud.objects.Room;

public abstract class Command {
	protected static int USER = 0;   // basic user permissions
	protected static int ADMIN = 1;  //
	protected static int WIZARD = 2; //
	protected static int GOD = 3;    // Pff, such arrogant idiots we are! (anyway, max permissions)
	
	protected MUDServer parent;
	
	public Command() {
		this(null);
	}

	/**
	 * Construct a command object with a parent
	 * MUDServer. Cannot construct an ordinary Command
	 * object because it is an abstract class. This
	 * is effectively a dummy constructor for subclasses.
	 * 
	 * @param mParent
	 */
	public Command(MUDServer mParent) {
		this.parent = mParent;
	}
	
	/**
	 * execute
	 * 
	 * This is where the code that constitutes what the command
	 * does will go.
	 * 
	 * @param arg
	 * @param client
	 */
	public abstract void execute(String arg, Client client);
	
	/**
	 * getAcessLevel
	 * 
	 * returns the specified access permissions required to use
	 * the command
	 * 
	 * @return int representing access level
	 */
	public abstract int getAccessLevel();
	
	/**
	 * method that passes the thing to be sent for debugging to
	 * the MUDServer instance's send method, eliminating the
	 * need to prefix the command with 'parent.' anywhere but here.
	 * 
	 * @param toSend
	 */
	public void send(String toSend, Client client) {
		this.parent.send(toSend, client);
	}
	
	/**
	 * method that passes the thing to be sent for debugging to
	 * the MUDServer instance's send debug method, eliminating the
	 * need to prefix the command with 'parent.' anywhere but here.
	 * 
	 * NOTE: doesn't allow passing a debugLevel parameter...
	 * 
	 * @param toSend
	 */
	public void debug(String toSend) {
		this.parent.debug(toSend);
	}
	
	public String gameError(String source, int type) {
		return this.parent.gameError(source, type);
	}
	
	/**
	 * method that calls the parent MUDServer instance's getPlayer
	 * method on the object provided and returns either a Player object
	 * or null (when the object can't be used to get a player, or when
	 * no player was found)
	 * 
	 * @param object
	 * @return
	 */
	public Player getPlayer(Object object) {
		if( object instanceof String ) {
			return parent.getPlayer((String) object);
		}
		else if( object instanceof Client ) {
			return parent.getPlayer((Client) object);
		}
		
		return null;
	}
	
	/**
	 * method that calls the parent MUDServer instance's getRoom
	 * method on the object provided and returns either a Room object
	 * or null (when the object can't be used to get a room, or when
	 * no room was found)
	 * 
	 * @param object
	 * @return
	 */
	public Room getRoom(Object object) {
		if( object instanceof String ) {
			return parent.getRoom((String) object);
		}
		else if( object instanceof Client ) {
			return parent.getRoom((Client) object);
		}
		
		return null;
	}
}