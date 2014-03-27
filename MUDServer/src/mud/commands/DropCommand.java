package mud.commands;

import java.util.ArrayList;

import mud.MUDServer;

import mud.net.Client;

import mud.utils.Utils;

import mud.objects.Item;
import mud.objects.Player;
import mud.objects.Room;
import mud.objects.Room.Terrain;

/*
 * Copyright (c) 2012 Jeremy N. Harton
 * 
 * Released under the MIT License:
 * LICENSE.txt, http://opensource.org/licenses/MIT
 * 
 * NOTE: license provided with code controls, if any
 * changes are made to the one referred to.
 */

public class DropCommand extends Command {

	public DropCommand(MUDServer mParent) {
		super(mParent);
	}

	@Override
	public void execute(String arg, Client client) {
		// get player, room objects to work with
		Player player = getPlayer(client);
		Room room = getRoom( player.getLocation() );
		Item item;
		
		ArrayList<Item> inventory = player.getInventory();

		// get the integer value, if there is one, as the argument
        final int dbref = Utils.toInt(arg, -1);

		// get the object the argument refers to: by name (if it's in the calling player's inventory), or by dbref#
		// should be done by searching the player's inventory for the object and if there is such an object, drop it on the floor.
		for (int i = 0; i < inventory.size(); i++)
		{			
			item = inventory.get(i);
			
			final String itemName = item.getName();

			// if there is a name or dbref match from the argument in the inventory
			if ( itemName.equals(arg) || itemName.contains(arg) && !arg.equals("") || item.getDBRef() == dbref )
			{
				debug(itemName + " true");
				// move object from player inventory to floor
				inventory.remove(item);
				
				if( room.getTerrain() == Terrain.SKY ) {
					int id = Utils.toInt( (String) room.getProperty("dropto"), -1 );
					Room room1 = getRoom( id );
					
					if( room1 != null ) {
						item.setLocation(room.getDBRef());
						room1.addItem(item);
						
						send("You drop " + colors(itemName, "yellow") + " and it falls toward the ground...", client);
					}
					else {
						item.setLocation( -1 );
					}
				}
				else {
					item.setLocation(room.getDBRef());
					room.addItem(item);
					// check for silent flag to see if object's dbref name should be shown as well?
					// return message telling the player that they dropped the object
					send("You dropped " + colors(itemName, "yellow") + " on the floor.", client);
					// return message telling others that the player dropped the item?
				}
				
				return;
			}
		}
		
		send("You don't have that.", client);
	}

	@Override
	public int getAccessLevel() {
		return USER;
	}
}