package mud.commands;

import java.util.ArrayList;

import mud.Command;
import mud.Constants;
import mud.MUDServer;
import mud.ObjectFlag;
import mud.net.Client;
import mud.utils.Point;
import mud.utils.Utils;
import mud.objects.Item;
import mud.objects.Player;
import mud.objects.Room;
import mud.objects.Terrain;

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
		debug("Drop Command");
		
		// get player, room objects to work with
		Player player = getPlayer(client);
		Room room = getRoom( player.getLocation() );
		
		Item item;
		
		ArrayList<Item> inventory = player.getInventory();

		// get the integer value, if there is one, as the argument
        final int dbref = Utils.toInt(arg, -1);
        
        // dependent on limited access to the database
        if( dbref != -1 ) {
        	item = getItem(dbref);

        	if( item == null ) {
        		send("No such item.", client);
        		return;
        	}

        	final String itemName = item.getName();

        	if( item.getLocation() == player.getDBRef() ) {
        		inventory.remove( item );
        		item.setLocation( room.getDBRef() );
        		item.setPosition( player.getPosition() );
        		room.addItem( item );

        		send("You dropped " + colors(itemName, "yellow") + " on the floor.", client);
        	}
        }
        else {
        	// get the object the argument refers to: by name (if it's in the calling player's inventory), or by dbref#
        	// should be done by searching the player's inventory for the object and if there is such an object, drop it on the floor.
        	for (final Item item2 : inventory)
        	{			
        		final String itemName = item2.getName();

        		// if there is a name or dbref match from the argument in the inventory
        		if ( itemName.equals(arg) || itemName.contains(arg) && !arg.equals("") || item2.getDBRef() == dbref )
        		{
        			debug(itemName + " true");
        			
        			// remove object from player inventory
        			inventory.remove(item2);
        			
        			// move object from player inventory to ground
        			if( room.getTerrain() == Terrain.SKY ) {
        				int id = Utils.toInt( (String) room.getProperty("dropto"), -1 );
        				Room room1 = getRoom( id );

        				if( room1 != null ) {
        					final Point p = player.getPosition();
        					
        					item2.setLocation(room.getDBRef());
        					item2.setPosition(p.getX(), p.getY(), 0);
        					room1.addItem(item2);

        					send("You drop " + colors(itemName, "yellow") + " and it falls toward the ground...", client);
        				}
        				else {
        					item2.setLocation( -1 );
        					
        					send("You drop " + colors(itemName, "yellow") + " and it quickly disappears out of sight.", client);
        				}
        			}
        			else {
        				item2.setLocation( room.getDBRef() );
                		item2.setPosition( player.getPosition() );
                		room.addItem( item2 );
                		
        				// check for silent flag to see if object's dbref name should be shown as well?
                		if( !player.hasFlag(ObjectFlag.SILENT) ) {
                			send("You dropped " + colors(itemName, "yellow") + "(#" + item2.getDBRef() + ") on the floor.", client);
                		}
                		else {
                			send("You dropped " + colors(itemName, "yellow") + " on the floor.", client);
                		}
        				
        				// return message telling others that the player dropped the item?
                		// obviously we want the players in the current room that can see something
                		//for(final Player p
        			}
        			
        			return;
        		}
        	}
        	
        	send("You don't have that.", client);
        }
	}

	@Override
	public int getAccessLevel() {
		return Constants.USER;
	}
}