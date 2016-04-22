package mud.commands;

import mud.Command;
import mud.Constants;
import mud.MUDServer;
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
		
		for(final Player p : getPlayers()) {
			final String playerName = Utils.padRight( Utils.truncate(p.getName(), 16), 16);

			switch(p.getAccess()) {
			case Constants.SUPERUSER: send(playerName + "[SUPERUSER]", client); break;
			case Constants.WIZARD:    send(playerName + "[WIZARD]", client); break;
			case Constants.ADMIN:     send(playerName + "[ADMIN]", client); break;
			case Constants.BUILD:     send(playerName + "[BUILD]", client); break;
			default: break;
			}
		}
	}

	@Override
	public int getAccessLevel() {
		return Constants.USER;
	}
}