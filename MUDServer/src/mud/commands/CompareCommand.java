package mud.commands;

import mud.Command;
import mud.Constants;
import mud.MUDServer;
import mud.net.Client;
import mud.objects.Item;
import mud.objects.Player;
import mud.objects.Room;

/*
 * Copyright (c) 2012 Jeremy N. Harton
 * 
 * Released under the MIT License:
 * LICENSE.txt, http://opensource.org/licenses/MIT
 * 
 * NOTE: license provided with code controls, if any
 * changes are made to the one referred to.
 */

public class CompareCommand extends Command {
	public CompareCommand() {
		super("");
	}
	
	@Override
	public void execute(String arg, Client client) {
		// get player, room objects to work with
		Player player = getPlayer(client);
		Room room = getRoom( player.getLocation() );
		Item item1;
		Item item2;
	}

	@Override
	public int getAccessLevel() {
		return Constants.USER;
	}

}
