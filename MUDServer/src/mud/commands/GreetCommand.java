package mud.commands;

import mud.Constants;
import mud.MUDServer;
import mud.net.Client;
import mud.objects.Player;

/*
 * Copyright (c) 2012 Jeremy N. Harton
 * 
 * Released under the MIT License:
 * LICENSE.txt, http://opensource.org/licenses/MIT
 * 
 * NOTE: license provided with code controls, if any
 * changes are made to the one referred to.
 */

public class GreetCommand extends Command {
	
	public GreetCommand(final MUDServer mParent) {
		super(mParent);
	}

	@Override
	public void execute(final String arg, final Client client) {
		debug(arg);
		
		final Player current = getPlayer(client);
		debug("current: " + current.getName());
		
		// TODO need a way to identify players by something other than their name
		final Player player1 = getPlayer(arg);
		final Client client1 = player1.getClient();
		debug("player1: " + player1.getName());
		
		if( client == null ) {
			send("Game> That player is not logged-in", current.getClient());
			return;
		}
		
		if (!player1.getNames().contains(current.getName())) {
			player1.addName(current.getName());
			
			if (current.getNames().contains(player1.getName())) {
				send("You tell " + player1.getName() + " that your name is " + current.getName(), client);
			}
			else {
				send("You tell " + arg + " that your name is " + current.getName(), client);
			}
			
			send(current.getCName() + " tells you that their name is " + current.getName(), client1);
		}
		else {
			send("You've already greeted that player", client);
		}
	}
	
	@Override
	public int getAccessLevel() {
		return Constants.USER;
	}
}