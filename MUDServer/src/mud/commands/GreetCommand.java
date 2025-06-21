package mud.commands;

import mud.Command;
import mud.Constants;
import mud.net.Client;
import mud.objects.NPC;
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
		super("Greet another player/npc (this tells them your name with some specificity).");
	}

	@Override
	public void execute(final String arg, final Client client) {
		// TODO figure out whether we are tryng to greet an NPC and respond accordingly
		final Player self = getPlayer(client);
		
		boolean done = false;
		
		// TODO need to make this locality dependent
		final Player player = getPlayer(arg);		
		final NPC npc = getNPC(arg);

		debug("player:  " + self.getName());
		debug("player1: " + player.getName());
		
		if ( player != null ) {
			// TODO this is a poor test indeed
			if( player.getClient() == null ) {
				send("Game> That player is not logged-in", client);
				return;
			}

			if ( !player.knowsName( self.getName() ) ) {
				player.addName( self.getName() );
				
				String msg = "";
				
				// message to you
				if ( self.knowsName( player.getName() ) ) {
					msg = String.format("You tell %s that your name is %s.", player.getName(), self.getName());
				}
				else {
					msg = String.format("You tell %s that your name is %s.", arg, self.getName());
				}
				
				send( msg, client );
				
				// message to other player
				send(self.getCName() + " tells you that their name is " + self.getName() + ".", player.getClient());
			}
			else {
				send("You've already greeted that player.", client);
			}
		}
		else if ( npc != null ) {
			// TODO exists so NPC can greet you by name only if they know it
			if ( !npc.knowsName( self.getName() ) ) {
				npc.addName( self.getName() );
				
				String msg = "";
				
				// message to you
				if ( self.knowsName( npc.getName() ) ) {
					msg = String.format("You tell %s that your name is %s.", npc.getName(), self.getName());
				}
				else {
					msg = String.format("You tell %s that your name is %s.", arg, self.getName());
				}
				
				send( msg, client );
				
				// message to NPC?
				//send(self.getCName) + " tells you that their name is " + self.getName() + ".", player.getClient());
			}
			else {
				send("You've already greeted that npc?", client);
			}
		}
		else {
			send("There is no one by that name here.", client);
		}
	}

	@Override
	public int getAccessLevel() {
		return Constants.USER;
	}
}