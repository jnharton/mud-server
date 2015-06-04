package mud.commands;

import mud.Command;
import mud.Constants;
import mud.MUDObject;
import mud.MUDServer;
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
	
	public TeleportCommand(MUDServer mParent) {
		super(mParent);
	}

	@Override
	public void execute(String arg, Client client) {
		final Player player = getPlayer(client);

		String[] args = arg.split("=");

		final int destination;
		MUDObject target = null;
		
		if(args.length > 1) {
			target = getObject(args[0]);
			
			destination = Utils.toInt(args[1], -1);
		}
		else {
			destination = Utils.toInt(arg, -1);
		}
		
		boolean success = false; // does the destination exist?

		// try to find the room, by dbref or by name
		Room room = (destination != -1) ? getRoom(destination) : getRoom(arg);

		if (room != null) {
			success = true;
		}

		// if we found the room, send the player there
		if ( success ) {
			if( target == null) {
				getRoom(client).removeListener(player); // remove listener

				send("Teleporting to " + room.getName() + "... ", client);
				
				player.setLocation(room.getDBRef());
				player.setPosition(0, 0);
				
				send("Done.", client);
				
				room = getRoom(client);
				//parent.look(room, client);

				room.addListener(player); // add listener
			}
			else {
				if( target instanceof Player ) {
					final Player player1 = (Player) target; 
					getRoom(player1.getClient()).removeListener(player1);
				}
				
				send("Teleporting " + target.getName() + " to " + room.getName() + "... ", client);
				
				target.setLocation(room.getDBRef());
				target.setPosition(0, 0);
				
				send("Done.", client);
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