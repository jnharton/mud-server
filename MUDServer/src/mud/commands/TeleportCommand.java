package mud.commands;

import java.util.Arrays;

import mud.Command;
import mud.Constants;
import mud.MUDObject;
import mud.TypeFlag;
import mud.net.Client;
import mud.objects.Item;
import mud.objects.Player;
import mud.objects.Room;
import mud.utils.Utils;

/*
 * Copyright (c) 2012 Jeremy N. Harton
 * 
 * Released under the MIT License:
 * LICENSE.txt, http://opensource.org/licenses/MIT
 * 
 * NOTE: license provided with code controls, if any
 * changes are made to the one referred to.
 */

public class TeleportCommand extends Command {
	// Syntax:
	// @tel <dbref> -> '@tel 4'
	// @tel <object>=<dbref> -> '@tel Iridan=4'
	
	public TeleportCommand() {
		super("");
	}
	
	@Override
	public void execute(final String arg, final Client client) {
		final Player player = getPlayer(client);
		final Room room = getRoom( player.getLocation() );
		
		String[] args = arg.split("=");
		
		System.out.println( Arrays.asList(args) );
		
		Room dest = null;

		MUDObject target = null;
		
		final int destination;
		
		boolean no_target = true;
		boolean valid_target = false;
		boolean destination_exists = false;
		
		if(args.length > 1) {
			no_target = false;
			
			target = getObject(args[0]);
			destination = Utils.toInt(args[1], -1);
		}
		else {
			destination = Utils.toInt(arg, -1);
		}
		
		//
		if( target != null ) valid_target = true;
		
		// try to find the room, by dbref or by name
		if( destination != -1 ) dest = getRoom( destination );
		else                    dest = getRoom(arg);

		if (dest != null) destination_exists = true;

		// if we found the room, send the player there
		if ( destination_exists ) {
			if( no_target ) {
				room.removeListener(player); // remove listener

				send("Teleporting to " + dest.getName() + "... ", client);
				
				player.setLocation( dest.getDBRef() );
				player.setPosition(0, 0);
				
				send("Done.", client);

				dest.addListener(player); // add listener
			}
			else if( valid_target ) {
				if( target != null ) {
					send("Teleporting " + target.getName() + " to " + dest.getName() + "... ", client);
					
					if( target.isType(TypeFlag.PLAYER) ) {
						final Player p = (Player) target;
						final Room r = getRoom( p.getLocation() );
						
						r.removeListener(p);
						
						target.setLocation( dest.getDBRef() );
						target.setPosition(0, 0);
						
						final Room r2 = getRoom( p.getLocation() );
						
						r2.addListener(p);
						
						send("You were teleported.", p.getClient());
					}
					else if( target.isType(TypeFlag.ITEM) ) {
						int tl = target.getLocation();
						
						Room r1 = getRoom( tl );
						Room r2 = getRoom( destination );
						
						r1.removeItem( (Item) target);
						r2.addItem( (Item) target );
						
						target.setLocation( dest.getDBRef() );
						target.setPosition(0, 0);
					}
					
					send("Done.", client);
				}
			}
			else send("Invalid Target.", client);
		}
		else {
			send("Teleport failed.", client);
		}
	}

	@Override
	public int getAccessLevel() {
		return Constants.WIZARD;
	}
}