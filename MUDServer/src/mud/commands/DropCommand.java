package mud.commands;

import java.util.ArrayList;

import mud.MUDServer;

import mud.net.Client;

import mud.objects.Item;
import mud.objects.Player;
import mud.objects.Room;

public class DropCommand extends Command {

	public DropCommand(MUDServer mParent) {
		super(mParent);
	}

	@Override
	public void execute(String arg, Client client) {
		// get player, room objects to work with
		Player player = parent.getPlayer(client);
		Room room = parent.getRoom(client);
		Item item;
		
		ArrayList<Item> inventory = player.getInventory();

		// get the integer value, if there is one, as the argument
		Integer dbref = -1;

		try {
			dbref = Integer.parseInt(arg);
		}
		catch(NumberFormatException nfe) {
			debug("Exception(TAKE): " + nfe.getMessage());
			debug("DBRef (NOT!): " + dbref);
		}
		
		// get the object the argument refers to: by name (if it's in the calling player's inventory), or by dbref#
		// should be done by searching the player's inventory for the object and if there is such an object, drop it on the floor.
		for(int i = 0; i < inventory.size(); i++)
		{			
			item = inventory.get(i);

			// if there is a name or dbref match from the argument in the inventory
			if( item.getName().equals(arg) == true || item.getName().contains(arg) == true || item.getDBRef() == dbref )
			{
				debug(item.getName() + " true");
				// move object from player inventory to floor
				item.setLocation(room.getDBRef());
				room.contents1.add(item);
				inventory.remove(item);
				// check for silent flag to see if object's dbref name should be shown as well?
				// return message telling the player that they dropped the object
				send("You dropped " + parent.colors(item.getName(), "yellow") + " on the floor.", client);
				// return message telling others that the player dropped the item?
				break;
			}
		}
	}

	@Override
	public int getAccessLevel() {
		return USER;
	}
}