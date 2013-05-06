package mud.commands;

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

public class AdminCommand extends Command {

	public AdminCommand(MUDServer mParent) {
		super(mParent);
	}

	@Override
	public void execute(String arg, Client client) {
		final String testpass = Utils.hash(arg);
		final String realpass = MUDServer.admin_pass;
		
		final Player player = getPlayer(client);
		
		if (testpass.equals(realpass)) {
			parent.send("Game> correct admin password, changing access to ADMIN", client);
			
			player.setAccess(ADMIN);
			
            try {
                parent.addToStaffChannel(player);
            } catch (Exception ex) {
                parent.send("Staff chat channel doesn't exist.", client);
            }
		}
		else {
			player.setAccess(USER);
			parent.removefromStaffChannel(player);
		}
	}

	@Override
	public int getAccessLevel() {
		return ADMIN;
	}
}