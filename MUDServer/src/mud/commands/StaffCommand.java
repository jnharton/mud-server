package mud.commands;

import mud.Command;
import mud.Constants;
import mud.net.Client;
import mud.objects.Player;
import mud.utils.Utils;

public class StaffCommand extends Command {
	public StaffCommand() {
		super("lists the staff members who are currently connected/logged-in");
	}

	@Override
	public void execute(String arg, Client client) {
		send("Staff", client);
		send("----------------------------------------", client);
		
		String accessLevel = "";

		for(final Player p : getPlayers()) {
			final String playerName = Utils.padRight( Utils.truncate(p.getName(), 16), 16);
			
			boolean staff = true;
			
			switch(p.getAccess()) {
			case Constants.SUPERUSER: accessLevel = "SUPERUSER"; break;
			case Constants.WIZARD:    accessLevel = "WIZARD";    break;
			case Constants.ADMIN:     accessLevel = "ADMIN";     break;
			case Constants.BUILD:     accessLevel = "BUILD";     break;
			default:                  staff = false;             break;
			}

			if( staff ) send(playerName + "[" + accessLevel + "]", client);
		}
	}

	@Override
	public int getAccessLevel() {
		return Constants.USER;
	}
}