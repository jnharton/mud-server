package mud.commands;

import mud.Command;
import mud.Constants;
import mud.MUDObject;
import mud.net.Client;
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

		MUDObject target = null;
		boolean no_target = true;
		
		final int destination;
		
		if(args.length > 1) {
			no_target = false;
			
			target = getObject(args[0]);
			
			destination = Utils.toInt(args[1], -1);
		}
		else {
			destination = Utils.toInt(arg, -1);
		}
		
		boolean success = false; // does the destination exist?

		// try to find the room, by dbref or by name
		Room room1 = (destination != -1) ? getRoom(destination) : getRoom(arg);

		if (room1 != null) {
			success = true;
		}

		// if we found the room, send the player there
		if ( success ) {
			if( no_target ) {
				room.removeListener(player); // remove listener

				send("Teleporting to " + room1.getName() + "... ", client);
				
				player.setLocation(room1.getDBRef());
				player.setPosition(0, 0);
				
				send("Done.", client);

				room1.addListener(player); // add listener
				
				//send(look(room1, player), client);
			}
			else {
				if( target != null ) {
					if( target instanceof Player ) {
						final Player p = (Player) target;
						final Room r = getRoom( p.getLocation() );
						
						r.removeListener(p);
					}
					
					send("Teleporting " + target.getName() + " to " + room1.getName() + "... ", client);

					target.setLocation(room.getDBRef());
					target.setPosition(0, 0);

					send("Done.", client);
					
					if( target instanceof Player ) {
						final Player p = (Player) target;
						final Room r = getRoom( p.getLocation() );
						
						r.addListener(p);
						
						//send(look(r, p), client);
					}
				}
				else send("Invalid Target.", client);
			}
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