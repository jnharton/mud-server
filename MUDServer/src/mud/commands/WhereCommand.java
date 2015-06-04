package mud.commands;

import java.util.List;

import mud.Command;
import mud.Constants;
import mud.MUDServer;
import mud.net.Client;
import mud.objects.Player;
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

public class WhereCommand extends Command {
	
	public WhereCommand(MUDServer mParent) {
		super(mParent);
	}

	@Override
	public void execute(String arg, Client client) {
		int n = 0;
		
		Player current = getPlayer(client);
		final List<String> names = current.getNames();
		final String currName = current.getName();
		
		
		send("Player     Class     S Race      Idle Status Location", client);
		send("-------------------------------------------------------------------------------------------------", client);
		for (Player player : getPlayers())
		{
			try {
				String name = player.getName();             // name ( 10 characters )
				String cname = player.getCName();           // cname ( ? characters )
				//String title = player.getTitle();         // title ( 8 characters )
				String playerClass = player.getPClass().getName();
				String playerGender = "" + player.getGender();
				String race = player.getRace().toString();
				String ustatus = player.getStatus();        // status ( 3 characters )
				int location = player.getLocation();        // room # ( 5 characters ) (max. 99999?)
				String room = getRoom(location).getName(); // truncate to 24 characters
				String locString;
				if (player.hasEffect("invisibility")) { locString = "INVISIBLE"; }
				else { locString = room + " (#" + player.getLocation() + ")"; }
				String idle = player.getIdleString();
				
				
				// example output:
				//
				// Player     Class     S Race      Idle Location
				// --------------------------------------------------------------
				// Nathan      None      N None      ---- Red Dragon Inn
				// --------------------------------------------------------------
				if (names.contains(name) || currName.equals(name)) {
					send(Utils.padRight(name, 10) + " " + Utils.padRight(playerClass, 9) + " " + Utils.padRight(playerGender, 1)
							+ Utils.padRight(race, 9) + " " + Utils.padRight(idle, 4) + " " + Utils.padRight(ustatus, 6) + " " + locString, client);
				}
				else {
					send(Utils.padRight(cname, 10) + " "+ Utils.padRight(playerClass, 9) + " " +  Utils.padRight(playerGender, 1)
							+ " " + Utils.padRight(race, 9) + " " + Utils.padRight(idle, 4) + " " + Utils.padRight(ustatus, 6) + " " + locString, client);
				}
				
				n++;
			}
			catch (NullPointerException npe) {
				npe.printStackTrace();
			}
		}
		send("-------------------------------------------------------------------------------------------------", client);
		
		send(n + " players currently online.", client);
	}

	@Override
	public int getAccessLevel() {
		return Constants.USER;
	}
}