package mud.commands;

import mud.Command;
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
	public GreetCommand() {
		super("Greet another player (this tells them your name with some specificity).");
	}

	@Override
	public void execute(final String arg, final Client client) {
		final Player player = getPlayer(client);
		final Player player1 = getPlayer(arg);

		debug("player:  " + player.getName());
		debug("player1: " + player1.getName());

		if( player1.getClient() == null ) {
			send("Game> That player is not logged-in", client);
			return;
		}

		if ( !player1.knowsName( player.getName() ) ) {
			player1.addName( player.getName() );

			if ( player.knowsName( player1.getName() ) ) {
				send("You tell " + player1.getName() + " that your name is " + player.getName() + ".", client);
			}
			else {
				send("You tell " + arg + " that your name is " + player.getName() + ".", client);
			}

			send(player.getCName() + " tells you that their name is " + player.getName() + ".", player1.getClient());
		}
		else {
			send("You've already greeted that player.", client);
		}
	}

	@Override
	public int getAccessLevel() {
		return Constants.USER;
	}
}