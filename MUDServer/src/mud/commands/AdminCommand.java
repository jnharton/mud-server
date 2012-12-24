package mud.commands;

import mud.MUDServer;
import mud.net.Client;
import mud.objects.Player;
import mud.utils.Utils;

public class AdminCommand extends Command {

	public AdminCommand(MUDServer mParent) {
		super(mParent);
	}

	@Override
	public void execute(String arg, Client client) {
		String testpass = Utils.hash(arg);
		String realpass = MUDServer.admin_pass;
		
		if( testpass.equals(realpass) == true ) {
			parent.send("Game> correct admin password, changing access to ADMIN", client);
			
			Player player = parent.getPlayer(client);
			
			player.setAccess(ADMIN);
			
			parent.getChatChannel(MUDServer.STAFF_CHANNEL).addListener(player);
		}
		else {
			Player player = parent.getPlayer(client);
			
			player.setAccess(USER);
			
			parent.getChatChannel(MUDServer.STAFF_CHANNEL).removeListener(player);
		}
	}

	@Override
	public int getAccessLevel() {
		return ADMIN;
	}
}