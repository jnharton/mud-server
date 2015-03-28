package mud.commands;

import mud.Command;
import mud.Constants;
import mud.MUDServer;
import mud.objects.Player;
import mud.net.Client;

/*
 * Copyright (c) 2012 Jeremy N. Harton
 * 
 * Released under the MIT License:
 * LICENSE.txt, http://opensource.org/licenses/MIT
 * 
 * NOTE: license provided with code controls, if any
 * changes are made to the one referred to.
 */

public class AccessCommand extends Command {

	public AccessCommand(MUDServer mParent) {
		super(mParent);
	}

	@Override
	public void execute(String arg, Client client) {
		// syntax: access <player>=<access level> (access level denoted by integer -- see CONSTANTS)
		String[] args = arg.split("=");

		if (args.length > 0) {
			
			Player player;
			
			if( args[0].equalsIgnoreCase("me") ) {
				player = getPlayer(client);
			}
			else {
				 player = getPlayer(args[0]);
			}

			if (player != null) { // if we have a valid player
				if (args.length > 1) { // if we have specified a new access level
					//Integer aL = player.getAccess();
					Integer aL = Constants.USER;
					
					aL = Constants.permissionMap.get(args[1].toUpperCase());

					if( aL == null ) {
						try {
							aL = Integer.parseInt(args[1]); // get new access value (integer)
						}
						catch(NumberFormatException nfe) {
							send("Invalid access level!", client); // non-numerical input and non-real numbers probably won't work
							aL = Constants.USER;
						}
					}

					player.setAccess(aL);                                                            // set access to new value (integer)
					
					send(player.getName() + "'s access level set to " + player.getAccess(), client); // tell us to what the player's access was set
				}
				else { // if there wasn't any argument other than player name
					send(player.getName() + "'s access level is " + player.getAccess(), client);
				}
			}
			else { // error
				if( args[0].equalsIgnoreCase("#levels") ) {
					send("GOD: 4, WIZARD: 3, ADMIN: 2, BUILD: 1, USER: 0", client);
				}
				else {
					send("No such player!", client);
				}
			}
		}
		else {
			send(gameError("@access", 1), client); // Invalid Syntax Error
		}
	}

	@Override
	public int getAccessLevel() {
		return Constants.SUPERUSER;
	}
}