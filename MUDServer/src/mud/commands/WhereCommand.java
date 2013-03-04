package mud.commands;

import mud.MUDServer;

import mud.net.Client;

import mud.objects.Player;
import mud.utils.Utils;

public class WhereCommand extends Command {
	
	public WhereCommand(MUDServer mParent) {
		super(mParent);
	}

	@Override
	public void execute(String arg, Client client) {
		int n = 0;
		
		send("Player     Class     S Race      Idle Status Location", client);
		send("-------------------------------------------------------------------------------------------------", client);
		for (Player player : parent.getPlayers())
		{
			try {
				String name = player.getName(); // need to limit name to 10 characters
				String cname = player.getCName();
				//String title = player.getTitle(); // need to limit title to 8 characters
				String playerClass = player.getPClass().getName();
				String playerGender = player.getGender().toString();
				String race = player.getPRace().toString();
				String ustatus = player.getStatus(); // need to limit status to 3 characters
				int location = player.getLocation(); // set room # limit to 5 characters (max. 99999)
				String room = parent.getRoom(location).getName(); // truncate to 24 characters
				String locString;
				if (player.hasEffect("invisibility")) { locString = "INVISIBLE"; }
				else { locString = room + " (#" + player.getLocation() + ")"; }
				String idle = player.getIdleString();

				Player current = getPlayer(client);

				if (current.getNames().contains(name) || current.getName().equals(name)) {
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
		return USER;
	}
}