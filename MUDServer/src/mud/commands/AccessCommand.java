package mud.commands;

import mud.MUDServer;
import mud.objects.Player;
import mud.net.Client;

public class AccessCommand extends Command {

	public AccessCommand(MUDServer mParent) {
		super(mParent);
	}

	@Override
	public void execute(String arg, Client client) {
		// syntax: access <player>=<access level denoted by integer -- 0 is none/1 is admin>
		String[] args = arg.split("=");

		if (args.length > 0) {
			Player player = getPlayer(args[0]);

			if (player != null) { // if we have a valid player
				if (args.length > 1) { // if we have specified a new access level
					Integer aL = player.getAccess();

					try {
						aL = Integer.parseInt(args[1]); // get new access value (integer)
					}
					catch(NumberFormatException nfe) {
						send("Invalid access level!", client); // non-numerical input and non-real numbers probably won't work
					}

					player.setAccess(aL);                                                            // set access to new value (integer)
					send(player.getName() + "'s access level set to " + player.getAccess(), client); // tell us to what the player's access was set
				}
				else { // if there wasn't any argument other than player name
					send(player.getName() + "'s access level is " + player.getAccess(), client);
				}
			}
			else { send("No such player!", client); } // error
		}
		else {
			send(gameError("@access", 1), client); // Invalid Syntax Error
		}
	}

	@Override
	public int getAccessLevel() {
		return GOD;
	}
}